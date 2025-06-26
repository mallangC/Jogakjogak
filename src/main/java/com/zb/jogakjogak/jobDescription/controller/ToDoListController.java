package com.zb.jogakjogak.jobDescription.controller;

import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.jobDescription.domain.requestDto.BulkToDoListUpdateRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.CreateToDoListRequestDto;
import com.zb.jogakjogak.jobDescription.domain.requestDto.UpdateToDoListRequestDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListGetByCategoryResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListResponseDto;
import com.zb.jogakjogak.jobDescription.service.ToDoListService;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/jds/{jd_id}/to-do-lists")
public class ToDoListController {

    private final ToDoListService toDoListService;

    /**
     * 특정 JD에 새로운 ToDoList를 생성합니다.
     *
     * @param jdId       경로 변수로 전달되는 ToDoList를 추가할 JD의 고유 ID
     * @param dto        요청 본문에 포함된, 생성할 ToDoList의 상세 정보
     * @param customUser 현재 인증된 사용자 정보
     * @return 생성된 ToDoList의 상세 정보와 성공 메시지를 포함하는 응답.
     */
    @PostMapping
    public ResponseEntity<HttpApiResponse<ToDoListResponseDto>> createToDoList(
            @PathVariable("jd_id") Long jdId,
            @RequestBody @Valid CreateToDoListRequestDto dto,
            @AuthenticationPrincipal CustomOAuth2User customUser) {

        String memberName = customUser.getName();
        ToDoListResponseDto response = toDoListService.createToDoList(jdId, dto, memberName);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new HttpApiResponse<>(
                        response,
                        "체크리스트 추가 완료",
                        HttpStatus.CREATED
                )
        );
    }

    /**
     * 특정 JD에 속한 기존 ToDoList의 내용을 수정합니다.
     *
     * @param jdId        경로 변수로 전달되는 ToDoList가 속한 JD의 고유 ID
     * @param toDoListId  경로 변수로 전달되는 수정할 ToDoList의 고유 ID
     * @param toDoListDto 요청 본문에 포함된, 업데이트할 ToDoList의 상세 정보 (수정할 필드만 포함 가능)
     * @param customUser  현재 인증된 사용자 정보
     * @return 수정된 ToDoList의 상세 정보와 성공 메시지를 포함하는 응답.
     */
    @PatchMapping("/{toDoListId}")
    public ResponseEntity<HttpApiResponse<ToDoListResponseDto>> updateToDoList(
            @PathVariable("jd_id") Long jdId,
            @PathVariable Long toDoListId,
            @RequestBody @Valid UpdateToDoListRequestDto toDoListDto,
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
     * 특정 JD에 속한 단일 ToDoList의 상세 정보를 조회합니다.
     *
     * @param jdId       경로 변수로 전달되는 ToDoList가 속한 JD의 고유 ID
     * @param toDoListId 경로 변수로 전달되는 조회할 ToDoList의 고유 ID
     * @param customUser 현재 인증된 사용자 정보
     * @return 조회된 ToDoList의 상세 정보와 성공 메시지를 포함하는 응답
     */
    @GetMapping("/{toDoListId}")
    public ResponseEntity<HttpApiResponse<ToDoListResponseDto>> getToDoList(
            @PathVariable("jd_id") Long jdId,
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
     * 특정 JD에 속한 단일 ToDoList를 삭제합니다.
     *
     * @param jdId       경로 변수로 전달되는 ToDoList가 속한 JD의 고유 ID
     * @param toDoListId 경로 변수로 전달되는 삭제할 ToDoList의 고유 ID
     * @param customUser 현재 인증된 사용자 정보
     * @return 빈 데이터와 성공 메시지를 포함하는 응답
     */
    @DeleteMapping("/{toDoListId}")
    public ResponseEntity<HttpApiResponse<String>> deleteToDoList(
            @PathVariable("jd_id") Long jdId,
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
     * 특정 JD에 속한 특정 카테고리의 모든 ToDoList들을 조회합니다.
     *
     * @param jdId       경로 변수로 전달되는 ToDoList가 속한 JD의 고유 ID
     * @param category   쿼리 파라미터로 전달되는 조회할 ToDoList의 카테고리 (예: STRUCTURAL_COMPLEMENT_PLAN)
     * @param customUser 현재 인증된 사용자 정보
     * @return 조회된 ToDoList들의 목록과 성공 메시지를 포함하는 응답.
     */
    @GetMapping
    public ResponseEntity<HttpApiResponse<ToDoListGetByCategoryResponseDto>> getToDoListsByCategory(
            @PathVariable("jd_id")  Long jdId,
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
     * 특정 JD에 속한 여러 ToDoList를 일괄적으로 생성, 수정, 삭제합니다.
     * 이 엔드포인트를 통해 복수 개의 ToDoList를 동시에 관리할 수 있습니다.
     *
     * @param jdId       경로 변수로 전달되는 ToDoList들이 속한 JD의 고유 ID
     * @param dto        요청 본문에 포함된, 일괄 업데이트/생성/삭제할 ToDoList 정보 (카테고리, 생성/수정 목록, 삭제 ID 목록 포함)
     * @param customUser 현재 인증된 사용자 정보
     * @return 일괄 작업 후 해당 카테고리에 속하는 모든 ToDoList들의 목록과 성공 메시지를 포함하는 응답.
     */
    @PutMapping("/bulk-update")
    public ResponseEntity<HttpApiResponse<ToDoListGetByCategoryResponseDto>> bulkUpdateToDoLists(
            @PathVariable("jd_id")  Long jdId,
            @RequestBody BulkToDoListUpdateRequestDto dto,
            @AuthenticationPrincipal CustomOAuth2User customUser) {
        String memberName = customUser.getName();
        toDoListService.bulkUpdateToDoLists(jdId, dto, memberName);
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        toDoListService.getToDoListsByJdAndCategory(jdId, dto.getCategory(), memberName),
                        "다중 투두리스트 수정 성공",
                        HttpStatus.OK
                )
        );
    }
}
