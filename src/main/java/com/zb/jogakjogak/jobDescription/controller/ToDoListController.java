package com.zb.jogakjogak.jobDescription.controller;

import com.zb.jogakjogak.global.HttpApiResponse;
import com.zb.jogakjogak.jobDescription.domain.requestDto.ToDoListDto;
import com.zb.jogakjogak.jobDescription.domain.responseDto.ToDoListResponseDto;
import com.zb.jogakjogak.jobDescription.service.ToDoListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody @Valid ToDoListDto toDoListDto) {

        ToDoListResponseDto response = toDoListService.createToDoList(jdId, toDoListDto);

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
            @RequestBody @Valid ToDoListDto toDoListDto) {
        ToDoListResponseDto response = toDoListService.updateToDoList(jdId, toDoListId, toDoListDto);
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
            @PathVariable Long toDoListId) {
        return ResponseEntity.ok().body(
                new HttpApiResponse<>(
                        toDoListService.getToDoList(jdId, toDoListId),
                        "체크리스트 조회 성공",
                        HttpStatus.OK
                )
        );
    }
}
