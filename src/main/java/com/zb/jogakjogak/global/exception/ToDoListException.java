package com.zb.jogakjogak.global.exception;

import lombok.Getter;

@Getter
public class ToDoListException extends RuntimeException {

    private final ToDoListErrorCode toDoListErrorCode;

    public ToDoListException(ToDoListErrorCode errorCode) {
        super(errorCode.getMessage());
        this.toDoListErrorCode = errorCode;
    }
}
