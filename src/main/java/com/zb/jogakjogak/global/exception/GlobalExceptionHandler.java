package com.zb.jogakjogak.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JDException.class)
    public ResponseEntity<ErrorResponse> handleJDException(JDException e) {
        JDErrorCode jdErrorCode = e.getErrorCode();
        ErrorResponse response = new ErrorResponse(jdErrorCode.name(), jdErrorCode.getMessage());
        return new ResponseEntity<>(response, jdErrorCode.getHttpStatus());
    }
    @ExceptionHandler(ResumeException.class)
    public ResponseEntity<ErrorResponse> handleResumeException(ResumeException e) {
        ResumeErrorCode resumeErrorCode = e.getErrorCode();
        ErrorResponse response = new ErrorResponse(resumeErrorCode.name(), resumeErrorCode.getMessage());
        return new ResponseEntity<>(response, resumeErrorCode.getHttpStatus());
    }
    @ExceptionHandler(ToDoListException.class)
    public ResponseEntity<ErrorResponse> handleToDoListException(ToDoListException e) {
        ToDoListErrorCode toDoListErrorCode = e.getErrorCode();
        ErrorResponse response = new ErrorResponse(toDoListErrorCode.name(), toDoListErrorCode.getMessage());
        return new ResponseEntity<>(response, toDoListErrorCode.getHttpStatus());
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(AuthException e) {
        MemberErrorCode memberErrorCode = e.getMemberErrorCode();
        ErrorResponse response = new ErrorResponse(memberErrorCode.name(), memberErrorCode.getMessage());
        return new ResponseEntity<>(response, memberErrorCode.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorResponse response = new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

