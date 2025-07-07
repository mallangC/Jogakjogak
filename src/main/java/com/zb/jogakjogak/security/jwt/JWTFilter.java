package com.zb.jogakjogak.security.jwt;

import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.security.Role;
import com.zb.jogakjogak.security.Token;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.entity.Member;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private static final Set<String> WHITELIST = Set.of(
            "/actuator/health",
            "/api/member/reissue",
            "/api/member/logout",
            "/login/oauth2/code/kakao",
            "/oauth2",
            "/login/oauth2",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/webjars"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1. 화이트리스트 경로 처리 (토큰 검증 건너뛰기)
        if (isWhitelisted(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = extractAccessToken(request);

        // 2. 토큰이 없는 경우 처리: AuthException 대신 다음 필터로 넘기거나 401 반환
        if (accessToken == null) {
            // System.out.println("No access token provided for path: " + path); // 디버깅용
            // 일반적으로 여기서 401 Unauthorized를 반환하거나
            // Spring Security의 기본 인증 메커니즘에 맡겨서 로그인 페이지로 리다이렉트되도록 합니다.
            // 현재는 AuthException을 던지도록 되어있을텐데, 이를 제거합니다.
            // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Token is missing or invalid."); // 예시
            filterChain.doFilter(request, response); // 토큰이 없으면 다음 필터로 넘김 (SecurityContext에 Authentication이 없으므로 401 응답이 발생할 것)
            return;
        }

        // 3. 토큰 유효성 검증
        try {
            jwtUtil.validateToken(accessToken, Token.ACCESS_TOKEN);
        } catch (AuthException e) { // jwtUtil.validateToken에서 던지는 예외를 여기서 catch
            // System.out.println("Invalid token for path: " + path + ", Error: " + e.getMessage()); // 디버깅용
            // 유효하지 않은 토큰일 경우 401 Unauthorized 응답
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }

        // 토큰이 유효할 경우에만 다음 로직 실행
        String userName = jwtUtil.getUserName(accessToken);
        String role = jwtUtil.getRole(accessToken);

        Member member = Member.builder()
                .username(userName)
                .password(null)
                .role(Role.valueOf(role))
                .build();

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(member);
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(String path) {
        return WHITELIST.stream().anyMatch(path::startsWith);
    }

    private String extractAccessToken(HttpServletRequest request){
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return  authorization.split(" ")[1];
        }
        return null;
    }
}
