package com.zb.jogakjogak.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResumeErrorCode {

    NOT_FOUND_RESUME(HttpStatus.NOT_FOUND, "해당 이력서를 찾을 수 없습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN,"해당 이력서에 대한 권한이 없습니다." ),
    RESUME_NOT_FOUND_PLEASE_REGISTER(HttpStatus.NOT_FOUND, "이력서를 찾을 수 없습니다. 이력서를 등록해주세요."),
    NOT_ENTERED_CAREER(HttpStatus.BAD_REQUEST, "신입이 아니라면 경력을 입력해주세요."),
    ANALYSIS_ALLOWED_ONCE_WITHOUT_RESUME(HttpStatus.BAD_REQUEST, "이력서 없이 채용공고 분석은 1번만 가능합니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
