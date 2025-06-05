package com.zb.jogakjogak.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum JDErrorCode {

    FAILED_ANALYSIS_REQUEST(HttpStatus.BAD_REQUEST,"분석에 실패했습니다." + " 확인하고 다시 시도해주세요"),
    INVALID_API_REQUEST(HttpStatus.BAD_REQUEST, "클라이언트 오류 처리" ),
    API_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류" ),
    FAILED_JSON_PROCESS(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 파싱 오류")
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
