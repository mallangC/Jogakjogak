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
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED,"해당 JD에 대한 권한이 없습니다." )
    , JD_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST,"JD는 20개 이상 만들 수 없습니다." ),
    FAILED_ANALYSIS_REQUEST_TEXT_LENGTH_EXCEED(HttpStatus.BAD_REQUEST, "분석 중 오류가 발생했습니다.(todolist 제목 글자수제한)"),
    INVALID_RESUME_CONTENT(HttpStatus.BAD_REQUEST,"이력서 내용이 유효하지 않거나 의미 없는 반복 문자를 포함합니다."),
    INVALID_JOB_DESCRIPTION_CONTENT(HttpStatus.BAD_REQUEST,"채용 공고 내용이 유효하지 않거나 의미 없는 반복 문자를 포함합니다."),
    INVALID_JOB_NAME(HttpStatus.BAD_REQUEST, "직무 이름이 유효하지 않습니다."),
    AI_ANALYSIS_UNAVAILABLE(HttpStatus.BAD_REQUEST, "유효하지 않거나 분석하기 어려운 입력 내용입니다. 정확한 이력서와 채용 공고 내용을 다시 제공해주세요." ),
    JD_LIMIT_EXCEEDED_WITHOUT_RESUME(HttpStatus.BAD_REQUEST, "이력서가 없는 경우 채용공고분석은 최대 1개까지 가능합니다." );

    private final HttpStatus httpStatus;
    private final String message;
}
