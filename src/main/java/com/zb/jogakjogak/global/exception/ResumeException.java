package com.zb.jogakjogak.global.exception;

import lombok.Getter;

@Getter
public class ResumeException extends RuntimeException {

    private final ResumeErrorCode errorCode;

    public ResumeException(ResumeErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
