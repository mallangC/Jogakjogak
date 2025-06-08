package com.zb.jogakjogak.global.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    NOT_FOUND_TOKEN(HttpStatus.NOT_FOUND, "존재하지 않는 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    NOT_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "refresh 토큰이 아닙니다." ),



    ;
    private final HttpStatus httpStatus;
    private final String message;
}
