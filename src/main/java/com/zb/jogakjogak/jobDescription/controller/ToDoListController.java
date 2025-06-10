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

    @PostMapping
    public ResponseEntity<ToDoListResponseDto> createToDoList(
            @PathVariable Long jdId,
            @RequestBody @Valid ToDoListDto toDoListDto) {

        ToDoListResponseDto response = toDoListService.createToDoList(jdId, toDoListDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
