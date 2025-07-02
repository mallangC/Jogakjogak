package com.zb.jogakjogak.jobDescription.service;

import com.zb.jogakjogak.global.exception.*;
import com.zb.jogakjogak.jobDescription.domain.requestDto.BulkToDoListUpdateRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.CreateToDoListRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListUpdateRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.UpdateToDoListRequestDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListGetByCategoryResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListResponseDto;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repository.JDRepository;
import com.zb.jogakjogak.jobDescription.repository.ToDoListRepository;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ToDoListService {

    private final JDRepository jdRepository;
    private final ToDoListRepository toDoListRepository;
    private final MemberRepository memberRepository;

    private static final Set<ToDoListType> ALLOWED_CATEGORIES = EnumSet.of(
            ToDoListType.STRUCTURAL_COMPLEMENT_PLAN,
            ToDoListType.CONTENT_EMPHASIS_REORGANIZATION_PROPOSAL,
            ToDoListType.SCHEDULE_MISC_ERROR
    );

    /**
     * 특정 JD에 새로운 ToDoList를 추가하는 메서드
     *
     * @param jdId        ToDoList를 추가할 JD의 ID
     * @param toDoListDto 추가할 ToDoList의 정보
     * @param memberName  로그인한 유저
     * @return 새로 생성된 ToDoList의 응답 DTO
     */
    @Transactional
    public ToDoListResponseDto createToDoList(Long jdId, CreateToDoListRequestDto toDoListDto, String memberName) {

        JD jd = getAuthorizedJd(jdId, memberName);
        ToDoListType newCategory = toDoListDto.getCategory();

        validateAllowedCategory(newCategory);

        long currentCountForCategory =
                toDoListRepository.countToDoListsByJdIdAndCategory(jd.getId(), newCategory);

        if (currentCountForCategory + 1 > 10) {
            throw new ToDoListException(ToDoListErrorCode.TODO_LIST_LIMIT_EXCEEDED_FOR_CATEGORY);
        }

        ToDoList toDoList = ToDoList.createToDoList(toDoListDto, jd);
        ToDoList savedToDoList = toDoListRepository.save(toDoList);
        return ToDoListResponseDto.fromEntity(savedToDoList);
    }

    /**
     * 특정 JD에 속한 ToDoList를 수정하는 메서드
     *
     * @param jdId        ToDoList가 속한 JD의 ID
     * @param toDoListId  수정할 ToDoList의 ID
     * @param toDoListDto 업데이트할 ToDoList의 정보
     * @param memberName  로그인한 유저
     * @return 수정된 ToDoList의 응답 DTO
     */
    @Transactional
    public ToDoListResponseDto updateToDoList(Long jdId, Long toDoListId, UpdateToDoListRequestDto toDoListDto, String memberName) {

        JD jd = getAuthorizedJd(jdId, memberName);
        ToDoList toDoList = toDoListRepository.findToDoListWithJdByIdAndJdId(toDoListId, jd.getId())
                .orElseThrow(() -> new ToDoListException(ToDoListErrorCode.UNAUTHORIZED_ACCESS));
        toDoList.updateFromDto(toDoListDto);
        ToDoList updatedToDoList = toDoListRepository.save(toDoList);
        return ToDoListResponseDto.fromEntity(updatedToDoList);
    }

    /**
     * 특정 JD에 속한 ToDoList를 조회하는 메서드
     *
     * @param jdId       ToDoList가 속한 JD의 ID
     * @param toDoListId 조회할 ToDoList의 ID
     * @param memberName 로그인한 유저
     * @return 조회된 ToDoList의 응답 DTO
     */
    @Transactional(readOnly = true)
    public ToDoListResponseDto getToDoList(Long jdId, Long toDoListId, String memberName) {
        JD jd = getAuthorizedJd(jdId, memberName);
        ToDoList toDoList = toDoListRepository.findToDoListWithJdByIdAndJdId(toDoListId, jd.getId())
                .orElseThrow(() -> new ToDoListException(ToDoListErrorCode.UNAUTHORIZED_ACCESS));
        return ToDoListResponseDto.fromEntity(toDoList);
    }

    /**
     * 특정 JD에 속한 ToDoList를 삭제하는 메서드
     *
     * @param jdId       ToDoList가 속한 JD의 ID
     * @param toDoListId 조회할 ToDoList의 ID
     * @param memberName 로그인한 유저
     */
    @Transactional
    public void deleteToDoList(Long jdId, Long toDoListId, String memberName) {

        JD jd = getAuthorizedJd(jdId, memberName);
        ToDoList toDoList = toDoListRepository.findToDoListWithJdByIdAndJdId(toDoListId, jd.getId())
                .orElseThrow(() -> new ToDoListException(ToDoListErrorCode.UNAUTHORIZED_ACCESS));

        toDoListRepository.delete(toDoList);
    }

    /**
     * 회원과 JD의 권한을 확인하고, 유효한 JD 객체를 반환하는 헬퍼 메서드.
     */
    private JD getAuthorizedJd(Long jdId, String memberName) {
        Member member = memberRepository.findByUsername(memberName)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));

        return jdRepository.findJdWithMemberAndToDoListsByIdAndMemberId(jdId, member.getId())
                .orElseThrow(() -> new JDException(JDErrorCode.UNAUTHORIZED_ACCESS));
    }

    /**
     * 특정 JD에 속한 ToDoList들을 일괄적으로 수정, 생성, 삭제.
     *
     * @param jdId       ToDoList가 속한 JD의 ID
     * @param dto        ToDoList 수정 내용 (생성/수정/삭제 목록 포함)
     * @param memberName 로그인한 유저
     */
    @Transactional
    public void bulkUpdateToDoLists(Long jdId, BulkToDoListUpdateRequestDto dto, String memberName) {
        JD jd = getAuthorizedJd(jdId, memberName);

        ToDoListType targetCategory = dto.getCategory();
        if (targetCategory == null) {
            throw new ToDoListException(ToDoListErrorCode.CATEGORY_REQUIRED);
        }

        validateAllowedCategory(targetCategory);

        validateToDoListCount(jd.getId(), targetCategory, dto.getUpdatedOrCreateToDoLists(), dto.getDeletedToDoListIds());

        if (dto.getDeletedToDoListIds() != null && !dto.getDeletedToDoListIds().isEmpty()) {
            processDeletedToDoLists(jd, targetCategory, dto.getDeletedToDoListIds());
        }

        if (dto.getUpdatedOrCreateToDoLists() != null && !dto.getUpdatedOrCreateToDoLists().isEmpty()) {
            processUpdatedOrCreateToDoLists(jd, targetCategory, dto.getUpdatedOrCreateToDoLists());
        }

    }

    /**
     * 특정 JD에 속한 특정 카테고리의 ToDoList들을 조회하는 메서드
     *
     * @param jdId       ToDoList가 속한 JD의 ID
     * @param category   조회할 ToDoList의 카테고리
     * @param memberName 로그인한 유저
     * @return 조회된 ToDoList들의 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public ToDoListGetByCategoryResponseDto getToDoListsByJdAndCategory(Long jdId, ToDoListType category, String memberName) {
        JD jd = getAuthorizedJd(jdId, memberName);

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
     *
     * @param jd             권한이 검증된 JD 엔티티
     * @param targetCategory 현재 작업 대상 카테고리
     * @param dtoList        생성 또는 수정될 ToDoList DTO 목록
     */
    private void processUpdatedOrCreateToDoLists(JD jd, ToDoListType targetCategory, List<ToDoListUpdateRequestDto> dtoList) {
        for (ToDoListUpdateRequestDto dto : dtoList) {

            if (dto.getId() != null) {
                ToDoList updateToDoList = toDoListRepository.findToDoListWithJdByIdAndJdId(dto.getId(), jd.getId())
                        .orElseThrow(() -> new ToDoListException(ToDoListErrorCode.UNAUTHORIZED_ACCESS));

                if (!updateToDoList.getCategory().equals(targetCategory)) {
                    throw new ToDoListException(ToDoListErrorCode.TODO_LIST_NOT_BELONG_TO_JD);
                }
                updateToDoList.updateFromBulkUpdateToDoLists(dto, targetCategory);
                toDoListRepository.save(updateToDoList);
            } else {
                ToDoList newToDoList = ToDoList.builder()
                        .category(targetCategory)
                        .title(dto.getTitle())
                        .content(dto.getContent())
                        .memo("")
                        .isDone(dto.isDone())
                        .jd(jd)
                        .build();
                toDoListRepository.save(newToDoList);
            }
        }
    }

    /**
     * 일괄 업데이트 요청에서 삭제될 ToDoList들을 처리.
     *
     * @param jd             권한이 검증된 JD 엔티티
     * @param targetCategory 현재 작업 대상 카테고리
     * @param idsToDelete    삭제할 ToDoList ID 목록
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
     *
     * @param category 검증할 ToDoListType
     */
    private void validateAllowedCategory(ToDoListType category) {
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new ToDoListException(ToDoListErrorCode.INVALID_TODO_LIST_CATEGORY);
        }
    }

    /**
     * 특정 카테고리의 ToDoList 총 개수가 10개를 초과하는지 검증합니다.
     * 새로 생성될 항목, 삭제될 항목을 모두 고려하여 최종 개수를 예상합니다.
     *
     * @param jdId                현재 JD의 ID
     * @param targetCategory      대상 ToDoList 카테고리
     * @param updatedOrCreateList 생성 또는 수정될 ToDoList DTO 목록
     * @param deletedIds          삭제될 ToDoList ID 목록
     */
    private void validateToDoListCount(Long jdId, ToDoListType targetCategory,
                                       List<ToDoListUpdateRequestDto> updatedOrCreateList,
                                       List<Long> deletedIds) {
        long currentDbCount = toDoListRepository.countToDoListsByJdIdAndCategory(jdId, targetCategory);

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
