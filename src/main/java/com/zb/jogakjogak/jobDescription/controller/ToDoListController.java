package com.zb.jogakjogak.jobDescription.controller;

import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.jobDescription.domain.requestDto.BulkToDoListUpdateRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListDeleteResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListGetByCategoryResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListResponseDto;
import com.zb.jogakjogak.jobDescription.service.ToDoListService;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.entity.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jds/{jdId}/to-do-lists")
public class ToDoListController {

    private final ToDoListService toDoListService;

    /**
     * 특정 JD에 새로운 ToDoList를 추가하는 메서드
     *
     * @param jdId ToDoList를 추가할 JD의 ID
     * @param toDoListDto 추가할 ToDoList의 정보
     * @return 새로 생성된 ToDoList의 응답 DTO
     */
    @PostMapping
    public ResponseEntity<HttpApiResponse<ToDoListResponseDto>> createToDoList(
            @PathVariable Long jdId,
            @RequestBody @Valid ToDoListDto toDoListDto,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        String memberName = customUser.getName();
        ToDoListResponseDto response = toDoListService.createToDoList(jdId, toDoListDto, memberName);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new HttpApiResponse<>(
                        response,
                        "체크리스트 추가 완료",
                        HttpStatus.CREATED
                )
        );
    }

    /**
     * 특정 JD에 속한 ToDoList를 수정하는 메서드
     *
     * @param jdId        ToDoList가 속한 JD의 ID
     * @param toDoListId  수정할 ToDoList의 ID
     * @param toDoListDto 업데이트할 ToDoList의 정보
     * @return 수정된 ToDoList의 응답 DTO
     */
    @PatchMapping("/{toDoListId}")
    public ResponseEntity<HttpApiResponse<ToDoListResponseDto>> updateToDoList(
            @PathVariable Long jdId,
            @PathVariable Long toDoListId,
            @RequestBody @Valid ToDoListDto toDoListDto,
            @AuthenticationPrincipal CustomOAuth2User customUser) {
        String memberName = customUser.getName();
        ToDoListResponseDto response = toDoListService.updateToDoList(jdId, toDoListId, toDoListDto, memberName);
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        response,
                        "체크리스트 수정 완료",
                        HttpStatus.OK
                )
        );
    }

    /**
     * 특정 JD에 속한 ToDoList를 조회하는 메서드
     *
     * @param jdId ToDoList가 속한 JD의 ID
     * @param toDoListId 조회할 ToDoList의 ID
     * @return 조회된 ToDoList의 응답 DTO
     */
    @GetMapping("/{toDoListId}")
    public ResponseEntity<HttpApiResponse<ToDoListResponseDto>> getToDoList(
            @PathVariable Long jdId,
            @PathVariable Long toDoListId,
            @AuthenticationPrincipal CustomOAuth2User customUser) {
        String memberName = customUser.getName();
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        toDoListService.getToDoList(jdId, toDoListId, memberName),
                        "체크리스트 조회 성공",
                        HttpStatus.OK
                )
        );
    }

    /**
     * 특정 JD에 속한 ToDoList를 삭제하는 메서드
     *
     * @param jdId ToDoList가 속한 JD의 ID
     * @param toDoListId 조회할 ToDoList의 ID
     * @return 삭제된 ToDoList의 응답 DTO
     */
    @DeleteMapping("/{toDoListId}")
    public ResponseEntity<HttpApiResponse<String>> deleteToDoList(
            @PathVariable Long jdId,
            @PathVariable Long toDoListId,
            @AuthenticationPrincipal CustomOAuth2User customUser) {
        String memberName = customUser.getName();
        toDoListService.deleteToDoList(jdId, toDoListId, memberName);
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        "",
                        "체크리스트 삭제 성공",
                        HttpStatus.OK
                )
        );
    }

    /**
     * 특정 JD에 속한 특정 카테고리의 ToDoList들을 조회하는 메서드
     *
     * @param jdId ToDoList가 속한 JD의 ID
     * @param category 조회할 ToDoList의 카테고리 (STRUCTURAL_COMPLEMENT_PLAN 등)
     * @return 조회된 ToDoList들의 응답 DTO 리스트
     */
    @GetMapping
    public ResponseEntity<HttpApiResponse<ToDoListGetByCategoryResponseDto>> getToDoListsByCategory(
            @PathVariable Long jdId,
            @RequestParam(name = "category") ToDoListType category,
            @AuthenticationPrincipal CustomOAuth2User customUser) {
        String memberName = customUser.getName();
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        toDoListService.getToDoListsByJdAndCategory(jdId, category, memberName),
                        "카테고리별 투두리스트 조회 성공",
                        HttpStatus.OK
                )
        );
    }

    /**
     * 특정 JD에 속한  ToDoList들을 수정하는 메서드
     *
     * @param jdId ToDoList가 속한 JD의 ID
     * @param dto ToDoList 수정 내용
     * @return 수정된 ToDoList들의 응답 DTO 리스트
     */
    @PutMapping("/bulk-update")
    public ResponseEntity<HttpApiResponse<ToDoListGetByCategoryResponseDto>> bulkUpdateToDoLists(
            @PathVariable Long jdId,
            @RequestBody BulkToDoListUpdateRequestDto dto,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        String memberName = customUser.getName();
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        toDoListService.bulkUpdateToDoLists(jdId, dto, memberName),
                        "다중 투두리스트 수정 성공",
                        HttpStatus.OK
                )
        );
    }
}
