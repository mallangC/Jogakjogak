package com.zb.jogakjogak.jobDescription.service;

import com.zb.jogakjogak.global.exception.*;
import com.zb.jogakjogak.jobDescription.domain.requestDto.BulkToDoListUpdateRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListUpdateRequestDto;
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
     * @param memberName  로그인한 유저
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
     * @param memberName  로그인한 유저
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
     * @param memberName 로그인한 유저
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
     * @param memberName 로그인한 유저
     */
    public void deleteToDoList(Long jdId, Long toDoListId, String memberName) {

        JD jd = getAuthorizedJd(jdId, memberName);
        ToDoList toDoList = findToDoListAndValidateOwnership(jd, toDoListId);

        toDoListRepository.delete(toDoList);
    }

    /**
     * ToDoList를 ID로 찾고, 해당 JD에 속하는지, 그리고 특정 카테고리에 속하는지 검증합니다.
     *
     * @param jd         권한이 검증된 JD 엔티티
     * @param toDoListId 찾을 ToDoList의 ID
     * @return 찾아진 ToDoList 엔티티
     * @throws ToDoListException ToDoList를 찾을 수 없거나 해당 JD/카테고리에 속하지 않을 경우
     */
    private ToDoList findToDoListAndValidateOwnership(JD jd, Long toDoListId) {
        ToDoList toDoList = toDoListRepository.findById(toDoListId)
                .orElseThrow(() -> new ToDoListException(ToDoListErrorCode.TODO_LIST_NOT_FOUND));

        if (!toDoList.getJd().getId().equals(jd.getId())) {
            throw new ToDoListException(ToDoListErrorCode.TODO_LIST_NOT_BELONG_TO_JD);
        }
        return toDoList;
    }

    /**
     * 회원과 JD의 권한을 확인하고, 유효한 JD 객체를 반환하는 헬퍼 메서드.
     */
    private JD getAuthorizedJd(Long jdId, String memberName) {
        Member member = memberRepository.findByUsername(memberName)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));

        JD jd = jdRepository.findById(jdId)
                .orElseThrow(() -> new JDException(JDErrorCode.NOT_FOUND_JD));

        if (!Objects.equals(member.getId(), jd.getMember().getId())) {
            throw new JDException(JDErrorCode.UNAUTHORIZED_ACCESS);
        }
        return jd;
    }

    /**
     * 특정 JD에 속한 ToDoList들을 일괄적으로 수정, 생성, 삭제.
     *
     * @param jdId       ToDoList가 속한 JD의 ID
     * @param request    ToDoList 수정 내용 (생성/수정/삭제 목록 포함)
     * @param memberName 로그인한 유저
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
     * @param jdId       ToDoList가 속한 JD의 ID
     * @param category   조회할 ToDoList의 카테고리
     * @param memberName 로그인한 유저
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
    /**
     * 일괄 업데이트 요청에서 생성 또는 수정될 ToDoList들을 처리.
     *
     * @param jd             권한이 검증된 JD 엔티티
     * @param targetCategory 현재 작업 대상 카테고리
     * @param dtoList        생성 또는 수정될 ToDoList DTO 목록
     */
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
    /**
     * ToDoListUpdateRequestDto의 유효성 (JD ID 및 카테고리 일치)을 검증.
     *
     * @param jd             권한이 검증된 JD 엔티티
     * @param targetCategory 현재 작업 대상 카테고리
     * @param dto            검증할 ToDoListUpdateRequestDto
     */
    private void validateToDoListUpdateRequestDto(JD jd, ToDoListType targetCategory, ToDoListUpdateRequestDto dto) {
        if (dto.getJdId() == null || !dto.getJdId().equals(jd.getId())) {
            throw new ToDoListException(ToDoListErrorCode.TODO_LIST_NOT_BELONG_TO_JD);
        }
        if (dto.getCategory() == null || !dto.getCategory().equals(targetCategory)) {
            throw new ToDoListException(ToDoListErrorCode.TODO_LIST_CATEGORY_MISMATCH);
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
