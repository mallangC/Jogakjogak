package com.zb.jogakjogak.global.exception;

import lombok.Getter;

@Getter
public class SecurityException extends RuntimeException{
    private final MemberErrorCode memberErrorCode;

    public SecurityException(MemberErrorCode memberErrorCode) {
        super(memberErrorCode.getMessage());
        this.memberErrorCode = memberErrorCode;
    }
}
