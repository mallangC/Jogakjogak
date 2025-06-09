package com.zb.jogakjogak.global.exception;

import lombok.Getter;

@Getter
public class JDException extends RuntimeException {
    private final JDErrorCode errorCode;

    public JDException(JDErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
