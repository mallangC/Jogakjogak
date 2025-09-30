package com.zb.jogakjogak.global.exception;

import lombok.Getter;

@Getter
public class EventException extends RuntimeException {
    private final EventErrorCode errorCode;

    public EventException(EventErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
