package com.zb.jogakjogak.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ToDoListErrorCode {

    TODO_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "ToDoList를 찾을 수 없습니다." ),
    TODO_LIST_NOT_BELONG_TO_JD(HttpStatus.BAD_REQUEST, "해당 JD에 속하지 않는 ToDoList입니다." ),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
