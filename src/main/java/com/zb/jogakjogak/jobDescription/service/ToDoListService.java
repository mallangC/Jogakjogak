package com.zb.jogakjogak.jobDescription.service;

import com.zb.jogakjogak.global.exception.*;
import com.zb.jogakjogak.jobDescription.domain.requestDto.BulkToDoListUpdateRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListUpdateRequestDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListDeleteResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListGetByCategoryResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListResponseDto;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.ToDoList;
import com.zb.jogakjogak.jobDescription.repository.JDRepository;
import com.zb.jogakjogak.jobDescription.repository.ToDoListRepository;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ToDoListService {

    private final JDRepository jdRepository;
    private final ToDoListRepository toDoListRepository;
    private final MemberRepository memberRepository;

    /**
     * 특정 JD에 새로운 ToDoList를 추가하는 메서드
     *
     * @param jdId        ToDoList를 추가할 JD의 ID
     * @param toDoListDto 추가할 ToDoList의 정보
     * @return 새로 생성된 ToDoList의 응답 DTO
     */
    @Transactional
    public ToDoListResponseDto createToDoList(Long jdId, ToDoListDto toDoListDto, String memberName) {

        JD jd = getAuthorizedJd(jdId, memberName);
        ToDoList toDoList = ToDoList.fromDto(toDoListDto, jd);
        jd.addToDoList(toDoList);
        ToDoList savedToDoList = toDoListRepository.save(toDoList);
        return ToDoListResponseDto.fromEntity(savedToDoList);
    }

    /**
     * 특정 JD에 속한 ToDoList를 수정하는 메서드
     *
     * @param jdId        ToDoList가 속한 JD의 ID
     * @param toDoListId  수정할 ToDoList의 ID
     * @param toDoListDto 업데이트할 ToDoList의 정보
     * @return 수정된 ToDoList의 응답 DTO
     */
    @Transactional
    public ToDoListResponseDto updateToDoList(Long jdId, Long toDoListId, ToDoListDto toDoListDto, String memberName) {

        JD jd = getAuthorizedJd(jdId, memberName);
        ToDoList toDoList = findToDoListAndValidateOwnership(jd, toDoListId);
        toDoList.updateFromDto(toDoListDto);
        ToDoList updatedToDoList = toDoListRepository.save(toDoList);
        return ToDoListResponseDto.fromEntity(updatedToDoList);
    }

    /**
     * 특정 JD에 속한 ToDoList를 조회하는 메서드
     *
     * @param jdId       ToDoList가 속한 JD의 ID
     * @param toDoListId 조회할 ToDoList의 ID
     * @return 조회된 ToDoList의 응답 DTO
     */
    @Transactional(readOnly = true)
    public ToDoListResponseDto getToDoList(Long jdId, Long toDoListId, String memberName) {
        JD jd = getAuthorizedJd(jdId, memberName);
        ToDoList toDoList = findToDoListAndValidateOwnership(jd, toDoListId);
        return ToDoListResponseDto.fromEntity(toDoList);
    }

    /**
     * 특정 JD에 속한 ToDoList를 삭제하는 메서드
     *
     * @param jdId       ToDoList가 속한 JD의 ID
     * @param toDoListId 조회할 ToDoList의 ID
     */
    public void deleteToDoList(Long jdId, Long toDoListId, String memberName) {

        JD jd = getAuthorizedJd(jdId, memberName);
        ToDoList toDoList = findToDoListAndValidateOwnership(jd, toDoListId);

        toDoListRepository.delete(toDoList);
    }

    private ToDoList findToDoListAndValidateOwnership(JD jd, Long toDoListId) {
        ToDoList toDoList = toDoListRepository.findById(toDoListId)
                .orElseThrow(() -> new ToDoListException(ToDoListErrorCode.TODO_LIST_NOT_FOUND));

        if (!toDoList.getJd().getId().equals(jd.getId())) {
            throw new ToDoListException(ToDoListErrorCode.TODO_LIST_NOT_BELONG_TO_JD);
        }
        return toDoList;
    }

    private JD getAuthorizedJd(Long jdId, String memberName) {
        Member member = memberRepository.findByUserName(memberName)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));

        JD jd = jdRepository.findById(jdId)
                .orElseThrow(() -> new JDException(JDErrorCode.JD_NOT_FOUND));

        if (!Objects.equals(member.getId(), jd.getMember().getId())) {
            throw new JDException(JDErrorCode.UNAUTHORIZED_ACCESS);
        }
        return jd;
    }

    /**
     *특정 JD에 속한  ToDoList들을 수정하는 메서드
     *
     * @param jdId ToDoList가 속한 JD의 ID
     * @param request  ToDoList 수정 내용
     * @return 수정된 ToDoList들의 응답 DTO 리스트
     */
    @Transactional
    public ToDoListGetByCategoryResponseDto bulkUpdateToDoLists(Long jdId, BulkToDoListUpdateRequestDto request, String memberName) {
        JD jd = getAuthorizedJd(jdId, memberName);

        ToDoListType targetCategory = request.getCategory();
        if (targetCategory == null) {
            throw new ToDoListException(ToDoListErrorCode.CATEGORY_REQUIRED);
        }

        if (request.getUpdatedOrCreateToDoLists() != null) {
            processUpdatedOrCreateToDoLists(jd, targetCategory, request.getUpdatedOrCreateToDoLists());
        }

        if (request.getDeletedToDoListIds() != null && !request.getDeletedToDoListIds().isEmpty()) {
            processDeletedToDoLists(jd, targetCategory, request.getDeletedToDoListIds());
        }

        List<ToDoList> updatedListsInTargetCategory = toDoListRepository.findByJdAndCategory(jd, targetCategory);
        List<ToDoListResponseDto> responseDtoList = updatedListsInTargetCategory.stream()
                .map(ToDoListResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ToDoListGetByCategoryResponseDto.builder()
                .jdId(jdId)
                .category(request.getCategory())
                .toDoLists(responseDtoList)
                .build();
    }

    /**
     * 특정 JD에 속한 특정 카테고리의 ToDoList들을 조회하는 메서드
     *
     * @param jdId ToDoList가 속한 JD의 ID
     * @param category 조회할 ToDoList의 카테고리 (STRUCTURAL_COMPLEMENT_PLAN 등)
     * @return 조회된 ToDoList들의 응답 DTO 리스트
     */
    @Transactional(readOnly = true)
    public ToDoListGetByCategoryResponseDto getToDoListsByJdAndCategory(Long jdId, ToDoListType category, String memberName) {

        JD jd = getAuthorizedJd(jdId, memberName);

        List<ToDoList> toDoLists = toDoListRepository.findByJdAndCategory(jd, category);

        List<ToDoListResponseDto> responseDtoList = toDoLists.stream()
                .map(ToDoListResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ToDoListGetByCategoryResponseDto.builder()
                .jdId(jdId)
                .category(category)
                .toDoLists(responseDtoList)
                .build();
    }

    private void processUpdatedOrCreateToDoLists(JD jd, ToDoListType targetCategory, List<ToDoListUpdateRequestDto> dtoList) {
        for (ToDoListUpdateRequestDto dto : dtoList) {
            validateToDoListUpdateRequestDto(jd, targetCategory, dto);

            if (dto.getId() != null) {
                ToDoList toDoList = toDoListRepository.findById(dto.getId())
                        .orElseThrow(() -> new ToDoListException(ToDoListErrorCode.TODO_LIST_NOT_FOUND));

                if (!toDoList.getJd().getId().equals(jd.getId()) || !toDoList.getCategory().equals(targetCategory)) {
                    throw new ToDoListException(ToDoListErrorCode.TODO_LIST_NOT_BELONG_TO_JD);
                }
                toDoList.updateFromBulkUpdateToDoLists(dto);
            } else {
                ToDoList newToDoList = ToDoList.fromDto(dto, jd);
                toDoListRepository.save(newToDoList);
            }
        }
    }

    private void validateToDoListUpdateRequestDto(JD jd, ToDoListType targetCategory, ToDoListUpdateRequestDto dto) {
        if (dto.getJdId() == null || !dto.getJdId().equals(jd.getId())) {
            throw new ToDoListException(ToDoListErrorCode.TODO_LIST_NOT_BELONG_TO_JD);
        }
        if (dto.getCategory() == null || !dto.getCategory().equals(targetCategory)) {
            throw new ToDoListException(ToDoListErrorCode.TODO_LIST_CATEGORY_MISMATCH);
        }
    }

    private void processDeletedToDoLists(JD jd, ToDoListType targetCategory, List<Long> idsToDelete) {
        List<ToDoList> actualToDoListsToDelete = toDoListRepository.findAllById(idsToDelete);

        List<Long> verifiedIdsToDelete = actualToDoListsToDelete.stream()
                .filter(tl -> tl.getJd().getId().equals(jd.getId()) && tl.getCategory().equals(targetCategory))
                .map(ToDoList::getId)
                .collect(Collectors.toList());

        if (verifiedIdsToDelete.size() != idsToDelete.size()) {
            throw new ToDoListException(ToDoListErrorCode.TODO_LIST_NOT_BELONG_TO_JD);
        }
        toDoListRepository.deleteAllById(verifiedIdsToDelete);
    }
}
