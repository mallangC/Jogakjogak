package com.zb.jogakjogak.global.exception;

import lombok.Getter;

@Getter
public class ToDoListException extends RuntimeException {

    private final ToDoListErrorCode errorCode;

    public ToDoListException(ToDoListErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
