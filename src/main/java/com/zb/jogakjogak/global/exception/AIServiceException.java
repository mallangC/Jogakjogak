package com.zb.jogakjogak.global.exception;

import lombok.Getter;

@Getter
public class AIServiceException extends  RuntimeException{
    public AIServiceException(String message) {
        super(message);
    }

    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AIServiceException(Throwable cause) {
        super(cause);
    }
}
