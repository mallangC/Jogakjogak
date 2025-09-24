package com.zb.jogakjogak.jobDescription.service;

import com.zb.jogakjogak.global.exception.JDErrorCode;
import com.zb.jogakjogak.global.exception.JDException;
import com.zb.jogakjogak.global.exception.ToDoListErrorCode;
import com.zb.jogakjogak.global.exception.ToDoListException;
import com.zb.jogakjogak.jobDescription.domain.requestDto.*;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListGetByCategoryResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.UpdateIsDoneTodoListsResponseDto;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repository.JDRepository;
import com.zb.jogakjogak.jobDescription.repository.ToDoListRepository;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import com.zb.jogakjogak.security.entity.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ToDoListService {

    private final JDRepository jdRepository;
    private final ToDoListRepository toDoListRepository;

    private static final Set<ToDoListType> ALLOWED_CATEGORIES = EnumSet.of(
            ToDoListType.STRUCTURAL_COMPLEMENT_PLAN,
            ToDoListType.CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL,
            ToDoListType.SCHEDULE_MISC_ERROR
    );

    /**
     * 특정 JD에 새로운 ToDoList를 추가하는 메서드
     */
    @Transactional
    public ToDoListResponseDto createToDoList(Long jdId, CreateToDoListRequestDto toDoListDto, Member member) {

        JD jd = getAuthorizedJd(jdId, member);
        jd.setNotificationCount(0);
        ToDoListType newCategory = toDoListDto.getCategory();

        validateAllowedCategory(newCategory);

        long currentCountForCategory =
               jd.getToDoLists().stream().filter(
                       toDoList -> toDoList.getCategory() == newCategory
               ).count();

        if (currentCountForCategory + 1 > 10) {
            throw new ToDoListException(ToDoListErrorCode.TODO_LIST_LIMIT_EXCEEDED_FOR_CATEGORY);
        }

        ToDoList toDoList = ToDoList.createToDoList(toDoListDto, jd);
        ToDoList savedToDoList = toDoListRepository.save(toDoList);
        return ToDoListResponseDto.fromEntity(savedToDoList);
    }

    /**
     * 특정 JD에 속한 ToDoList를 수정하는 메서드
     */
    @Transactional
    public ToDoListResponseDto updateToDoList(Long jdId,
                                              Long toDoListId,
                                              UpdateToDoListRequestDto toDoListDto,
                                              Member member) {

        JD jd = getAuthorizedJd(jdId, member);
        jd.setNotificationCount(0);
        ToDoList toDoList = findToDoListInJd(jd, toDoListId);
        toDoList.updateFromDto(toDoListDto);
        return ToDoListResponseDto.fromEntity(toDoList);
    }

    /**
     * 특정 JD에 속한 ToDoList에 대한 완료 여부를 수정하는 메서드
     */
    @Transactional
    public ToDoListResponseDto toggleComplete(Long jdId, Long toDoListId, @Valid ToggleTodolistRequestDto dto, Member member) {
        JD jd = getAuthorizedJd(jdId, member);
        jd.setNotificationCount(0);
        ToDoList toDoList = findToDoListInJd(jd, toDoListId);
        toDoList.updateToDoListIsDone(dto.isDone());
        return ToDoListResponseDto.fromEntity(toDoList);
    }

    /**
     * 특정 JD에 속한 ToDoList를 조회하는 메서드
     */
    @Transactional(readOnly = true)
    public ToDoListResponseDto getToDoList(Long jdId, Long toDoListId, Member member) {
        JD jd = getAuthorizedJd(jdId, member);
        ToDoList toDoList = findToDoListInJd(jd, toDoListId);
        return ToDoListResponseDto.fromEntity(toDoList);
    }

    /**
     * 특정 JD에 속한 ToDoList를 삭제하는 메서드
     */
    @Transactional
    public void deleteToDoList(Long jdId, Long toDoListId, Member member) {

        JD jd = getAuthorizedJd(jdId, member);
        jd.setNotificationCount(0);
        ToDoList toDoList = findToDoListInJd(jd, toDoListId);
        toDoListRepository.delete(toDoList);
    }

    /**
     * 회원과 JD의 권한을 확인하고, 유효한 JD 객체를 반환하는 헬퍼 메서드.
     */
    private JD getAuthorizedJd(Long jdId, Member member) {
        return jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, member.getId())
                .orElseThrow(() -> new JDException(JDErrorCode.UNAUTHORIZED_ACCESS));
    }

    /**
     * JD 엔티티에 이미 로드된 ToDoList 컬렉션에서 특정 ToDoList를 찾는 헬퍼 메서드
     */
    private ToDoList findToDoListInJd(JD jd, Long toDoListId) {
        return jd.getToDoLists()
                .stream()
                .filter(tdl -> tdl.getId().equals(toDoListId))
                .findFirst()
                .orElseThrow(() -> new ToDoListException(ToDoListErrorCode.UNAUTHORIZED_ACCESS));
    }

    /**
     * 특정 JD에 속한 ToDoList들을 일괄적으로 수정, 생성, 삭제.
     */
    @Transactional
    public void bulkUpdateToDoLists(Long jdId, BulkToDoListUpdateRequestDto dto, Member member) {
        JD jd = getAuthorizedJd(jdId, member);
        jd.setNotificationCount(0);
        ToDoListType targetCategory = dto.getCategory();
        if (targetCategory == null) {
            throw new ToDoListException(ToDoListErrorCode.CATEGORY_REQUIRED);
        }

        validateAllowedCategory(targetCategory);
        validateToDoListCount(jd, targetCategory, dto.getUpdatedOrCreateToDoLists(), dto.getDeletedToDoListIds());

        if (dto.getDeletedToDoListIds() != null && !dto.getDeletedToDoListIds().isEmpty()) {
            processDeletedToDoLists(jd, targetCategory, dto.getDeletedToDoListIds());
        }

        if (dto.getUpdatedOrCreateToDoLists() != null && !dto.getUpdatedOrCreateToDoLists().isEmpty()) {
            processUpdatedOrCreateToDoLists(jd, targetCategory, dto.getUpdatedOrCreateToDoLists());
        }
    }

    /**
     * 특정 JD에 속한 특정 카테고리의 TodoList들의 완료여부를 수정하는 메서드
     */
    @Transactional
    public UpdateIsDoneTodoListsResponseDto updateIsDoneTodoLists(Long jdId, UpdateTodoListsIsDoneRequestDto dto, Member member) {
        getAuthorizedJd(jdId, member);

        List<ToDoList> updatedLists = new ArrayList<>();
        List<ToDoList> toDoLists = toDoListRepository.findAllById(dto.getToDoListIds());


        for (ToDoList toDoList : toDoLists) {
            toDoList.updateToDoListIsDone(dto.isDone());
            updatedLists.add(toDoList);
        }
        toDoListRepository.saveAll(updatedLists);

        List<ToDoListResponseDto> responseDtoList = updatedLists.stream()
                .map(ToDoListResponseDto::fromEntity)
                .collect(Collectors.toList());

        return UpdateIsDoneTodoListsResponseDto.builder()
                .toDoLists(responseDtoList)
                .isDone(dto.isDone())
                .build();
    }

    /**
     * 특정 JD에 속한 특정 카테고리의 ToDoList들을 조회하는 메서드
     */
    @Transactional(readOnly = true)
    public ToDoListGetByCategoryResponseDto getToDoListsByJdAndCategory(Long jdId, ToDoListType category, Member member) {
        JD jd = getAuthorizedJd(jdId, member);

        List<ToDoList> toDoLists = toDoListRepository.findToDoListsByJdIdAndCategoryWithJd(jd.getId(), category);

        List<ToDoListResponseDto> responseDtoList = toDoLists.stream()
                .map(ToDoListResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ToDoListGetByCategoryResponseDto.builder()
                .jdId(jdId)
                .category(category)
                .toDoLists(responseDtoList)
                .build();
    }

    /**
     * 일괄 업데이트 요청에서 생성 또는 수정될 ToDoList들을 처리.
     */
    private void processUpdatedOrCreateToDoLists(JD jd, ToDoListType targetCategory, List<ToDoListUpdateRequestDto> dtoList) {
        List<ToDoList> newToDos = new ArrayList<>();
        List<ToDoList> updatedToDos = new ArrayList<>();

        for (ToDoListUpdateRequestDto dto : dtoList) {
            if (dto.getId() != null) {
                ToDoList updateToDoList = toDoListRepository.findToDoListWithJdByIdAndJdId(dto.getId(), jd.getId())
                        .orElseThrow(() -> new ToDoListException(ToDoListErrorCode.UNAUTHORIZED_ACCESS));

                if (!updateToDoList.getCategory().equals(targetCategory)) {
                    throw new ToDoListException(ToDoListErrorCode.TODO_LIST_NOT_BELONG_TO_JD);
                }
                updateToDoList.updateFromBulkUpdateToDoLists(dto, targetCategory);
                updatedToDos.add(updateToDoList);
            } else {
                ToDoList newToDoList = ToDoList.builder()
                        .category(targetCategory)
                        .title(dto.getTitle())
                        .content(dto.getContent())
                        .memo("")
                        .isDone(dto.isDone())
                        .jd(jd)
                        .build();
                newToDos.add(newToDoList);
            }
        }
        // 루프가 끝난 후 한 번씩만 호출하여 불필요한 DB 접근을 줄입니다.
        toDoListRepository.saveAll(newToDos);
        toDoListRepository.saveAll(updatedToDos);
    }

    /**
     * 일괄 업데이트 요청에서 삭제될 ToDoList들을 처리.
     */
    private void processDeletedToDoLists(JD jd, ToDoListType targetCategory, List<Long> idsToDelete) {
        List<ToDoList> actualToDoListsToDelete = toDoListRepository.findAllByIdsWithJd(idsToDelete);
        List<Long> verifiedIdsToDelete = actualToDoListsToDelete.stream()
                .filter(tl -> tl.getJd().getId().equals(jd.getId()) && tl.getCategory().equals(targetCategory))
                .map(ToDoList::getId)
                .collect(Collectors.toList());

        if (verifiedIdsToDelete.size() != idsToDelete.size()) {
            throw new ToDoListException(ToDoListErrorCode.TODO_LIST_NOT_BELONG_TO_JD);
        }
        toDoListRepository.deleteAllById(verifiedIdsToDelete);
    }

    /**
     * 특정 ToDoListType이 허용되는 카테고리인지 검증합니다.
     */
    private void validateAllowedCategory(ToDoListType category) {
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new ToDoListException(ToDoListErrorCode.INVALID_TODO_LIST_CATEGORY);
        }
    }

    /**
     * 특정 카테고리의 ToDoList 총 개수가 10개를 초과하는지 검증합니다.
     * 새로 생성될 항목, 삭제될 항목을 모두 고려하여 최종 개수를 예상합니다.
     */
    private void validateToDoListCount(JD jd, ToDoListType targetCategory,
                                       List<ToDoListUpdateRequestDto> updatedOrCreateList,
                                       List<Long> deletedIds) {
        long currentDbCount = jd.getToDoLists().stream()
                .filter(toDoList -> toDoList.getCategory() == targetCategory)
                .count();
        long numToCreate = 0;
        if (updatedOrCreateList != null) {
            numToCreate = updatedOrCreateList.stream()
                    .filter(item -> item.getId() == null)
                    .count();
        }

        long numToDelete = 0;
        if (deletedIds != null) {
            numToDelete = deletedIds.size();
        }

        long potentialFinalCount = currentDbCount + numToCreate - numToDelete;

        if (potentialFinalCount > 10) {
            throw new ToDoListException(ToDoListErrorCode.TODO_LIST_LIMIT_EXCEEDED_FOR_CATEGORY);
        }
    }
}
