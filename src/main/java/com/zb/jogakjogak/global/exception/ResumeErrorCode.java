package com.zb.jogakjogak.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResumeErrorCode {

    NOT_FOUND_RESUME(HttpStatus.NOT_FOUND, "해당 이력서를 찾을 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
