package com.zb.jogakjogak.global;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler
    public HttpApiResponse<?> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("CustomException 발생: {}", e.getMessage());
        return createErrorResponse(errorCode.getStatus(), errorCode.getMessage());
        //return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDto<>(false, e.getMessage(), null));
    }
}
