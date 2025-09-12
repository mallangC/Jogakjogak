package com.zb.jogakjogak.jobDescription.controller;

import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.jobDescription.domain.requestDto.*;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListGetByCategoryResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListResponseDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.UpdateIsDoneTodoListsResponseDto;
import com.zb.jogakjogak.jobDescription.service.ToDoListService;
import com.zb.jogakjogak.jobDescription.type.ToDoListType;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Todolist 관리 API", description = "JD/이력서 분석으로 생성된 Todolist 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/jds/{jd_id}/to-do-lists")
public class ToDoListController {

    private final ToDoListService toDoListService;

    /**
     * 특정 JD에 새로운 ToDoList를 생성합니다.
     *
     */
    @Operation(summary = "특정 분석/카테고리의 Todolist 생성", description = "jd_id와 category를 통해 todolist를 생성합니다")
    @PostMapping
    public ResponseEntity<HttpApiResponse<ToDoListResponseDto>> createToDoList(
            @PathVariable("jd_id") Long jdId,
            @RequestBody @Valid CreateToDoListRequestDto dto,
            @AuthenticationPrincipal CustomOAuth2User customUser) {
        ToDoListResponseDto response = toDoListService.createToDoList(jdId, dto, customUser.getMember());

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
     */
    @Operation(summary = "특정 분석/카테고리의 Todolist 수정", description = "jd_id와 toDoList_id를 통해 todolist를 수정합니다")
    @PatchMapping("/{toDoListId}")
    public ResponseEntity<HttpApiResponse<ToDoListResponseDto>> updateToDoList(
            @PathVariable("jd_id") Long jdId,
            @PathVariable Long toDoListId,
            @RequestBody @Valid UpdateToDoListRequestDto toDoListDto,
            @AuthenticationPrincipal CustomOAuth2User customUser) {
        ToDoListResponseDto response = toDoListService.updateToDoList(jdId, toDoListId, toDoListDto, customUser.getMember());
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        response,
                        "체크리스트 수정 완료",
                        HttpStatus.OK
                )
        );
    }

    @Operation(summary = "특정 분석/카테고리의 Todolist 완료 여부 수정", description = "jd_id와 toDoList_id를 통해 todolist 완료 여부를 수정합니다")
    @PatchMapping("/{toDoListId}/isDone")
    public ResponseEntity<HttpApiResponse<ToDoListResponseDto>> toggleComplete(
            @PathVariable("jd_id") Long jdId,
            @PathVariable Long toDoListId,
            @RequestBody @Valid ToggleTodolistRequestDto toggleTodolist,
            @AuthenticationPrincipal CustomOAuth2User customUser) {
        ToDoListResponseDto response = toDoListService.toggleComplete(jdId, toDoListId, toggleTodolist, customUser.getMember());
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        response,
                        "체크리스트 완료 여부 수정 완료",
                        HttpStatus.OK
                )
        );
    }

    /**
     * 특정 JD에 속한 단일 ToDoList의 상세 정보를 조회합니다.

     */
    @Operation(summary = "특정 분석/카테고리의 Todolist 조회", description = "jd_id와 toDoList_id를 통해 todolist를 조회합니다")
    @GetMapping("/{toDoListId}")
    public ResponseEntity<HttpApiResponse<ToDoListResponseDto>> getToDoList(
            @PathVariable("jd_id") Long jdId,
            @PathVariable Long toDoListId,
            @AuthenticationPrincipal CustomOAuth2User customUser) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        toDoListService.getToDoList(jdId, toDoListId, customUser.getMember()),
                        "체크리스트 조회 성공",
                        HttpStatus.OK
                )
        );
    }

    /**
     * 특정 JD에 속한 단일 ToDoList를 삭제합니다.
     *
     */
    @Operation(summary = "특정 분석/카테고리의 Todolist 삭제", description = "jd_id와 toDoList_id를 통해 todolist를 삭제합니다")
    @DeleteMapping("/{toDoListId}")
    public ResponseEntity<HttpApiResponse<String>> deleteToDoList(
            @PathVariable("jd_id") Long jdId,
            @PathVariable Long toDoListId,
            @AuthenticationPrincipal CustomOAuth2User customUser) {
        toDoListService.deleteToDoList(jdId, toDoListId, customUser.getMember());
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
     */
    @Operation(summary = "특정 분석/카테고리의 모든 Todolist 조회", description = "jd_id와 category를 통해 해당되는 모든 todolist를 조회합니다")
    @GetMapping
    public ResponseEntity<HttpApiResponse<ToDoListGetByCategoryResponseDto>> getToDoListsByCategory(
            @PathVariable("jd_id")  Long jdId,
            @RequestParam(name = "category") ToDoListType category,
            @AuthenticationPrincipal CustomOAuth2User customUser) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        toDoListService.getToDoListsByJdAndCategory(jdId, category, customUser.getMember()),
                        "카테고리별 투두리스트 조회 성공",
                        HttpStatus.OK
                )
        );
    }

    /**
     * 특정 JD에 속한 여러 ToDoList를 일괄적으로 생성, 수정, 삭제합니다.
     * 이 엔드포인트를 통해 복수 개의 ToDoList를 동시에 관리할 수 있습니다.
     */
    @Operation(summary = "특정 분석/카테고리의 모든 Todolist 생성/수정/삭제", description = "jd_id와 category를 통해 생성, 수정, 삭제된 todolist 정보를 리스트 형식으로 받아 업데이트합니다")
    @PutMapping("/bulk-update")
    public ResponseEntity<HttpApiResponse<ToDoListGetByCategoryResponseDto>> bulkUpdateToDoLists(
            @PathVariable("jd_id")  Long jdId,
            @RequestBody BulkToDoListUpdateRequestDto dto,
            @AuthenticationPrincipal CustomOAuth2User customUser) {
        toDoListService.bulkUpdateToDoLists(jdId, dto, customUser.getMember());
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        toDoListService.getToDoListsByJdAndCategory(jdId, dto.getCategory(), customUser.getMember()),
                        "다중 투두리스트 수정 성공",
                        HttpStatus.OK
                )
        );
    }

    @Operation(summary = "여러 Todolist의 완료여부 일괄 수정", description = "jd_id를 통해 여러 Todolist의 완료여부를 일괄적으로 수정합니다")
    @PutMapping("/update-is-done")
    public ResponseEntity<HttpApiResponse<UpdateIsDoneTodoListsResponseDto>> updateIsDoneTodoLists(
            @PathVariable("jd_id")  Long jdId,
            @RequestBody UpdateTodoListsIsDoneRequestDto dto,
            @AuthenticationPrincipal CustomOAuth2User customUser){
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        toDoListService.updateIsDoneTodoLists(jdId, dto, customUser.getMember()),
                        "다중 투두리스트 완료여부 수정 성공",
                        HttpStatus.OK
                )
        );
    }
}
