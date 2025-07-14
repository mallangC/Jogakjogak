package com.zb.jogakjogak.security.oauth2;

import com.zb.jogakjogak.ga.service.GaMeasurementProtocolService;
import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import com.zb.jogakjogak.security.Token;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.entity.RefreshToken;
import com.zb.jogakjogak.security.jwt.JWTUtil;
import com.zb.jogakjogak.security.repository.MemberRepository;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GaMeasurementProtocolService gaService;
    private final MemberRepository memberRepository;
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;
    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication
    authentication) throws IOException, ServletException {

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String username = customOAuth2User.getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));
        Long userId = member.getId();
        String refreshToken = jwtUtil.createRefreshToken(userId, REFRESH_TOKEN_EXPIRATION, Token.REFRESH_TOKEN);

        addRefreshToken(username, userId, refreshToken);
        addSameSiteCookieAttribute(request, response, "refresh", refreshToken);

        String clientId = extractGaClientId(request);
        String gaUserId = member.getId().toString();
        String eventName = "user_login";

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("member_id", member.getId());
        eventParams.put("user_role", member.getRole());
        gaService.sendGaEvent(clientId, gaUserId, eventName, eventParams)
                .subscribe();

        // 환경에 따라 리다이렉트 URL 결정
        String redirectUrl;
        if (request.getServerName().contains("localhost")) {
            redirectUrl = "http://localhost:3000/login/oauth2/code/kakao";
        } else {
            redirectUrl = "https://www.jogakjogak.com/login/oauth2/code/kakao";
        }

        response.sendRedirect(redirectUrl);
    }

    private void addSameSiteCookieAttribute(HttpServletRequest request, HttpServletResponse response, String
    cookieName, String cookieValue) {
        String serverName = request.getServerName();
        boolean isLocal = serverName.contains("localhost");

        String cookieHeader;
        if (isLocal) {
            // 로컬 환경
            cookieHeader = String.format(
                "%s=%s; Max-Age=%d; Path=/; HttpOnly",
                cookieName,
                cookieValue,
                60 * 60 * 24 * 7
            );
        } else {
            // 프로덕션 환경
            cookieHeader = String.format(
                "%s=%s; Max-Age=%d; Path=/; Domain=.jogakjogak.com; HttpOnly; SameSite=None; Secure",
                cookieName,
                cookieValue,
                60 * 60 * 24 * 7
            );
        }

        response.addHeader("Set-Cookie", cookieHeader);
    }

    @Transactional
    private void addRefreshToken(String username, Long userId, String refresh) {
        refreshTokenRepository.findByUsername(username)
                .ifPresent(refreshTokenRepository::delete);
        RefreshToken refreshToken = RefreshToken.builder()
                .username(username)
                .token(refresh)
                .userId(userId)
                .expiration(LocalDateTime.now().plusSeconds(REFRESH_TOKEN_EXPIRATION / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * HttpServletRequest에서 GA의 _ga 쿠키 값을 추출하여 클라이언트 ID를 반환합니다.
     * _ga 쿠키는 "GA1.2.123456789.987654321" 형식이며,
     * 여기서 "123456789.987654321" 부분이 GA 클라이언트 ID입니다.
     *
     * @param request HttpServletRequest 객체
     * @return 추출된 GA 클라이언트 ID 또는 null (쿠키가 없거나 형식이 맞지 않을 경우)
     */
    private String extractGaClientId(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("_ga".equals(cookie.getName())) {
                    String gaCookieValue = cookie.getValue();
                    String[] parts = gaCookieValue.split("\\.");
                    if (parts.length >= 4) {
                        return parts[2] + "." + parts[3];
                    }
                }
            }
        }
        return null;
    }
}
