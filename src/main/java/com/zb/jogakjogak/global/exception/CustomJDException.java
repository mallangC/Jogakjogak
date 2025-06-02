package com.zb.jogakjogak.global.exception;

import lombok.Getter;

@Getter
public class CustomJDException extends RuntimeException {
    private final CustomJDErrorCode errorCode;

    public CustomJDException(CustomJDErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
