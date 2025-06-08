package com.zb.jogakjogak.security.jwt;

import com.zb.jogakjogak.security.entity.RefreshToken;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilter {
    private final RefreshTokenRepository refreshEntityRepository;
    private final JWTUtil jwtUtil;

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
        jwtUtil.validationToken(refreshToken);

        RefreshToken existingToken = refreshEntityRepository.findByRefreshToken(refreshToken);
        if (existingToken == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //로그아웃 진행
        refreshEntityRepository.deleteByRefreshToken(refreshToken);
        //Refresh 토큰 Cookie 값 0
        Cookie cookie = resetRefreshTokenInCookie();

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private boolean isLogoutRequest(HttpServletRequest request) {
        return "/member/logout".equals(request.getRequestURI()) && "POST".equals(request.getMethod());
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