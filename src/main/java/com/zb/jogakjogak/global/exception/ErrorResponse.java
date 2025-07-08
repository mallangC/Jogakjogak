package com.zb.jogakjogak.global.exception;

import lombok.Getter;

public record ErrorResponse(String errorCode, String message) {
}
