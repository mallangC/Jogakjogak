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
    FAILED_JSON_PROCESS(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 파싱 오류"),
    NOT_FOUND_JD(HttpStatus.NOT_FOUND, "JD를 찾을 수 없습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED,"해당 JD에 대한 권한이 없습니다." );

    private final HttpStatus httpStatus;
    private final String message;
}
