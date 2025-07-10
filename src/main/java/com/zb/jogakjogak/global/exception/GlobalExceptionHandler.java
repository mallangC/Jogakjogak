package com.zb.jogakjogak.global.exception;

import com.zb.jogakjogak.ga.service.GaMeasurementProtocolService;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final GaMeasurementProtocolService gaService;


    @ExceptionHandler(JDException.class)
    public ResponseEntity<ErrorResponse> handleJDException(JDException e, HttpServletRequest request) {
        JDErrorCode jdErrorCode = e.getErrorCode();
        ErrorResponse response = new ErrorResponse(jdErrorCode.name(), jdErrorCode.getMessage());

        sendGaErrorEvent(
                request,
                getUserIdFromSecurityContext(),
                "jd_exception",
                jdErrorCode.name(),
                jdErrorCode.getMessage(),
                jdErrorCode.getHttpStatus().value(),
                e
        );

        return new ResponseEntity<>(response, jdErrorCode.getHttpStatus());
    }

    @ExceptionHandler(ResumeException.class)
    public ResponseEntity<ErrorResponse> handleResumeException(ResumeException e, HttpServletRequest request) {
        ResumeErrorCode resumeErrorCode = e.getErrorCode();
        ErrorResponse response = new ErrorResponse(resumeErrorCode.name(), resumeErrorCode.getMessage());

        sendGaErrorEvent(
                request,
                getUserIdFromSecurityContext(),
                "resume_exception",
                resumeErrorCode.name(),
                resumeErrorCode.getMessage(),
                resumeErrorCode.getHttpStatus().value(),
                e
        );

        return new ResponseEntity<>(response, resumeErrorCode.getHttpStatus());
    }

    @ExceptionHandler(ToDoListException.class)
    public ResponseEntity<ErrorResponse> handleToDoListException(ToDoListException e, HttpServletRequest request) {
        ToDoListErrorCode toDoListErrorCode = e.getErrorCode();
        ErrorResponse response = new ErrorResponse(toDoListErrorCode.name(), toDoListErrorCode.getMessage());

        sendGaErrorEvent(
                request,
                getUserIdFromSecurityContext(),
                "todolist_exception",
                toDoListErrorCode.name(),
                toDoListErrorCode.getMessage(),
                toDoListErrorCode.getHttpStatus().value(),
                e
        );

        return new ResponseEntity<>(response, toDoListErrorCode.getHttpStatus());
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(AuthException e, HttpServletRequest request) {
        MemberErrorCode memberErrorCode = e.getMemberErrorCode();
        ErrorResponse response = new ErrorResponse(memberErrorCode.name(), memberErrorCode.getMessage());

        sendGaErrorEvent(
                request,
                getUserIdFromSecurityContext(),
                "auth_exception",
                memberErrorCode.name(),
                memberErrorCode.getMessage(),
                memberErrorCode.getHttpStatus().value(),
                e
        );

        return new ResponseEntity<>(response, memberErrorCode.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        ErrorResponse response = new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");

        sendGaErrorEvent(
                request,
                getUserIdFromSecurityContext(),
                "internal_server_error",
                "INTERNAL_SERVER_ERROR",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * GA 이벤트 전송을 위한 헬퍼 메서드
     */
    private void sendGaErrorEvent(HttpServletRequest request,
                                  String userId,
                                  String eventName,
                                  String errorCode,
                                  String errorMessage,
                                  int httpStatus,
                                  Exception originalException) {
        String clientId = getClientIdFromRequest(request);
        String apiPath = request.getRequestURI();
        String apiMethod = request.getMethod();

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("api_path", apiPath);
        eventParams.put("api_method", apiMethod);
        eventParams.put("error_code_custom", errorCode);
        eventParams.put("error_message_summary",
                errorMessage != null ? errorMessage.substring(0, Math.min(errorMessage.length(), 250)) : "No message");
        eventParams.put("http_status_code", httpStatus);
        eventParams.put("exception_type", originalException.getClass().getSimpleName());

        gaService.sendGaEvent(clientId, userId, eventName, eventParams).subscribe();
    }

    /**
     * 요청 헤더에서 클라이언트 ID 추출
     */
    private String getClientIdFromRequest(HttpServletRequest request) {
        String clientId = request.getHeader("X-Client-ID");
        if (clientId == null || clientId.isEmpty()) {
            return "backend_generated_error_" + UUID.randomUUID().toString();
        }
        return clientId;
    }

    // Spring Security Context에서 userId를 추출하는 헬퍼 메서드
    private String getUserIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                return customOAuth2User.getMember().getId().toString();
            }
        }
        return null;
    }
}
