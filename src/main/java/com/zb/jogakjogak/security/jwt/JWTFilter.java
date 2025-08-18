package com.zb.jogakjogak.security.jwt;

import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import com.zb.jogakjogak.security.Token;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.repository.MemberRepository;
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
    private final MemberRepository memberRepository;

    private static final Set<String> WHITELIST = Set.of(
            "/actuator/health",
            "/member/reissue",
            "/member/logout",
            "/login/oauth2/code",
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

        if (isWhitelisted(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = extractAccessToken(request);

        if (accessToken == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            String json = "{\"AuthException\": \"NOT_FOUND_TOKEN\", \"message\": \"토큰이 없습니다.\"}";
            response.getWriter().write(json);
            return;
        }

        try {
            jwtUtil.validateToken(accessToken, Token.ACCESS_TOKEN);
        } catch (AuthException e) { // jwtUtil.validateToken에서 던지는 예외를 여기서 catch
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            String json = "{\"errorCode\": \"UNAUTHORIZED\", \"message\": \"" + e.getMessage() + "\"}";
            response.getWriter().write(json);
            return;
        }

        // 토큰이 유효할 경우에만 다음 로직 실행
        String userName = jwtUtil.getUsername(accessToken);
        Member member = memberRepository.findByUsername(userName)
                .orElseThrow(() -> new AuthException(MemberErrorCode.NOT_FOUND_MEMBER));

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
