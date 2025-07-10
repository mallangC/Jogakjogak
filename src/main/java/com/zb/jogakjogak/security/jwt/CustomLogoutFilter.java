package com.zb.jogakjogak.security.jwt;

import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.security.Token;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilter {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTUtil jwtUtil;
    private static final String LOGOUT_URI = "/api/member/logout";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        doFilter((HttpServletRequest) request, (HttpServletResponse) response, filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        if (!isLogoutRequest(request)){
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = extractRefreshTokenFromCookie(request.getCookies());

        if (refreshToken == null) {
            log.warn("[LogoutFilter] Refresh token 쿠키가 없음");
            clearRefreshTokenInCookie(response);
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        try {
            jwtUtil.validateToken(refreshToken, Token.REFRESH_TOKEN);
        } catch (AuthException | JwtException | IllegalArgumentException e) {
            log.warn("[LogoutFilter] Refresh token 검증 실패 또는 username 추출 실패, 로그아웃 처리: {}", e.getMessage());
            clearRefreshTokenInCookie(response);
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        // 유효한 토큰인 경우 DB에서 refresh token 삭제
        Long userId = Long.parseLong(jwtUtil.getUserId(refreshToken));
        refreshTokenRepository.deleteByUserId(userId);
        log.info("[LogoutFilter] {} 사용자 로그아웃 처리 (refresh token 삭제)", userId);

        clearRefreshTokenInCookie(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private boolean isLogoutRequest(HttpServletRequest request) {
        return LOGOUT_URI.equals(request.getRequestURI()) && "POST".equals(request.getMethod());
    }

    private String extractRefreshTokenFromCookie(Cookie[] cookies) {
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void clearRefreshTokenInCookie(HttpServletResponse response){
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }
}