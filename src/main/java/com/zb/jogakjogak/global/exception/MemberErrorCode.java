package com.zb.jogakjogak.global.exception;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode {
    NOT_FOUND_TOKEN(HttpStatus.NOT_FOUND, "존재하지 않는 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    NOT_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "refresh 토큰이 아닙니다." ),
    TOKEN_TYPE_NOT_MATCH(HttpStatus.UNAUTHORIZED, "토큰 타입이 일치하지 않습니다." ),

    NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "존재하지않는 회원입니다."),

    MEMBER_WITHDRAWAL_FAIL(HttpStatus.EXPECTATION_FAILED, "회원탈퇴를 실패했습니다." ),
    ALREADY_HAVE_RESUME(HttpStatus.CONFLICT, "이미 이력서를 가지고 있습니다." )
    ;
    private final HttpStatus httpStatus;
    private final String message;
}
