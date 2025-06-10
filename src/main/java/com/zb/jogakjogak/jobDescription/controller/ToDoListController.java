package com.zb.jogakjogak.jobDescription.controller;

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
    public ResponseEntity<ToDoListResponseDto> createToDoList(
            @PathVariable Long jdId,
            @RequestBody @Valid ToDoListDto toDoListDto) {

        ToDoListResponseDto response = toDoListService.createToDoList(jdId, toDoListDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
