package com.zb.jogakjogak.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomJDErrorCode {

    FAILED_ANALYSIS_REQUEST(HttpStatus.BAD_REQUEST,"분석에 실패했습니다." +
            " 확인하고 다시 시도해주세요")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
