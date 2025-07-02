package com.zb.jogakjogak.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ToDoListErrorCode {

    TODO_LIST_NOT_BELONG_TO_JD(HttpStatus.BAD_REQUEST, "해당 JD에 속하지 않는 ToDoList입니다." ),
    CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "카테고리는 필수 입니다." ),
    TODO_LIST_LIMIT_EXCEEDED_FOR_CATEGORY(HttpStatus.BAD_REQUEST,"해당 카테고리의 ToDoList는 최대 10개까지 생성할 수 있습니다."),
    INVALID_TODO_LIST_CATEGORY(HttpStatus.BAD_REQUEST, "허용되지 않는 투두리스트 카테고리입니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN,"해당 투두리스트 대한 권한이 없습니다." );

    private final HttpStatus httpStatus;
    private final String message;
}
