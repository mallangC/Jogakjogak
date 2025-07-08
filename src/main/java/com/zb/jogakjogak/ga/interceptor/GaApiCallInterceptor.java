package com.zb.jogakjogak.ga.interceptor;

import com.zb.jogakjogak.ga.service.GaMeasurementProtocolService;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class GaApiCallInterceptor implements HandlerInterceptor {

    private final GaMeasurementProtocolService gaService;
    private static final String GA_API_CALL_EVENT_NAME = "api_call";
    private static final String START_TIME_ATTRIBUTE = "startTime";
    private static final String EXCEPTION_ATTRIBUTE = "exception";

    public GaApiCallInterceptor(GaMeasurementProtocolService gaService) {
        this.gaService = gaService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 이 단계에서는 예외를 아직 완벽히 포착하기 어려우므로 afterCompletion에서 최종 처리
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        long responseTime = (startTime != null) ? (System.currentTimeMillis() - startTime) : -1;

        String clientId = getClientIdFromRequest(request);
        String userId = getUserIdFromSecurityContext();
        String apiPath = request.getRequestURI();
        String apiMethod = request.getMethod();
        int httpStatusCode = response.getStatus();

        String apiStatus;
        String errorMessage = null;
        String errorCode = null;

        Exception actualException = (Exception) request.getAttribute(EXCEPTION_ATTRIBUTE);
        if (actualException == null && ex != null) {
            actualException = ex;
        }

        if (actualException != null) {
            apiStatus = "failure";
            errorMessage = actualException.getMessage();
            errorCode = getErrorCodeFromException(actualException);
        } else if (httpStatusCode >= 200 && httpStatusCode < 400) {
            apiStatus = "success";
        } else {
            apiStatus = "failure";
            errorCode = "HTTP_STATUS_ERROR_" + httpStatusCode;
        }

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("api_path", apiPath);
        eventParams.put("api_method", apiMethod);
        eventParams.put("api_status", apiStatus);
        eventParams.put("response_time_ms", responseTime);
        eventParams.put("http_status_code", httpStatusCode);

        if (errorMessage != null) {
            eventParams.put("error_message_summary", errorMessage.substring(0, Math.min(errorMessage.length(), 250))); // 메시지 길이 제한
        }
        if (errorCode != null) {
            eventParams.put("error_code_custom", errorCode); // 맞춤 측정기준 이름 일관성 유지
        }

        // userId 파라미터와 debugMode 파라미터 추가하여 sendGaEvent 호출
        gaService.sendGaEvent(clientId, userId, GA_API_CALL_EVENT_NAME, eventParams).subscribe();
    }

    // 요청 헤더에서 클라이언트 ID 추출 (프론트엔드에서 X-Client-ID 헤더로 보낸다고 가정)
    private String getClientIdFromRequest(HttpServletRequest request) {
        String clientId = request.getHeader("X-Client-ID"); // 예: React/Vue에서 Axios interceptor로 헤더에 추가
        if (clientId == null || clientId.isEmpty()) {
            // 클라이언트 ID가 없다면 임시 ID 생성 (사용자 여정 연결에는 한계)
            return "backend_generated_" + UUID.randomUUID().toString();
        }
        return clientId;
    }

    // Spring Security Context에서 userId를 추출하는 헬퍼 메서드
    private String getUserIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                // Member 객체가 null이 아니고, member.getId()의 반환값이 null이 아닐 때만 toString() 호출
                // CustomOAuth2User에 getMember() 메서드가 있다면 그것을 사용하는 것이 좋습니다.
                if (customOAuth2User.getMember() != null && customOAuth2User.getMember().getId() != null) {
                    return customOAuth2User.getMember().getId().toString(); // Member 객체의 실제 ID 필드 접근
                } else {
                    // member 또는 member.getId()가 null인 경우
                    System.err.println("Warning: Authenticated CustomOAuth2User has null Member or Member ID. Principal: " + customOAuth2User.getName());
                    return null; // userId를 null로 반환
                }
            }
            // 그 외 (일반 UserDetails, 또는 JWT 등에서 직접 사용자 ID를 Principal로 설정한 경우)
            // return authentication.getName(); // 사용자 이름(username)을 ID로 사용할 경우
        }
        return null; // 인증되지 않았거나 익명 사용자인 경우 userId 없음
    }

    // 예외 유형에 따라 특정 에러 코드를 반환하는 헬퍼 함수
    private String getErrorCodeFromException(Exception ex) {
        if (ex instanceof IllegalArgumentException) {
            return "INVALID_ARGUMENT";
        } else if (ex instanceof NullPointerException) {
            return "NULL_POINTER";
        }
        // ... 다른 예외 유형에 따른 코드 추가
        return "UNKNOWN_SERVER_ERROR";
    }
}
