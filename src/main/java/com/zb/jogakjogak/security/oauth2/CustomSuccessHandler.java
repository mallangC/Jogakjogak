package com.zb.jogakjogak.security.oauth2;

import com.zb.jogakjogak.security.Token;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.entity.RefreshToken;
import com.zb.jogakjogak.security.jwt.JWTUtil;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private static final long REFRESH_TOKEN_DAYS = 7;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String username = customOAuth2User.getName();
        String role = getRole(authentication);
        String refreshToken = jwtUtil.createJwt(username, role, REFRESH_TOKEN_DAYS * 24 * 60 * 60 * 1000L, Token.REFRESH_TOKEN);
        addRefreshToken(username, refreshToken);

        response.addCookie(createCookie("refresh", refreshToken));
        response.sendRedirect("http://localhost:3000/");
    }

    private String getRole(Authentication authentication){
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        return auth.getAuthority();
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 24 * 7);
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

    private void addRefreshToken(String userName, String refresh) {

        RefreshToken refreshToken = RefreshToken.builder()
                .username(userName)
                .token(refresh)
                .expiration(LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS))
                .build();
        refreshTokenRepository.save(refreshToken);
    }
}
