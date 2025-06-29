package com.zb.jogakjogak.security.jwt;

import com.zb.jogakjogak.security.Token;
import com.zb.jogakjogak.security.entity.RefreshToken;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilter {
    private final RefreshTokenRepository refreshEntityRepository;
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
        jwtUtil.validateToken(refreshToken, Token.REFRESH_TOKEN);

        Optional<RefreshToken> existingToken = refreshEntityRepository.findByToken(refreshToken);
        if (existingToken.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //로그아웃 진행
        refreshEntityRepository.deleteByToken(refreshToken);
        //Cookie 값 0
        Cookie cookie = resetRefreshTokenInCookie();
        response.addCookie(cookie);
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

    private Cookie resetRefreshTokenInCookie(){
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        return cookie;
    }
}