package com.zb.jogakjogak.global.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException{
    private final MemberErrorCode memberErrorCode;

    public AuthException(MemberErrorCode memberErrorCode) {
        super(memberErrorCode.getMessage());
        this.memberErrorCode = memberErrorCode;
    }
}
