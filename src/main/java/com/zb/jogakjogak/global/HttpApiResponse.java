package com.zb.jogakjogak.global;

import org.springframework.http.HttpStatus;

public record HttpApiResponse<T>(T data, String message, HttpStatus status) {
}