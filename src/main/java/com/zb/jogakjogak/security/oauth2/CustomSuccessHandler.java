package com.zb.jogakjogak.security.oauth2;

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
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
  
        addRefreshToken(username, refreshToken);
        addSameSiteCookieAttribute(request, response, "refresh", refreshToken);
  
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.println("""
            <html>
              <head><meta charset='UTF-8'></head>
              <body>
                <script>
                  window.location.href = 'https://www.jogakjogak.com/login/oauth2/code/kakao';
                </script>
              </body>
            </html>
        """);
        writer.flush();
    }

    private String getRole(Authentication authentication){
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        return auth.getAuthority();
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
                "%s=%s; Max-Age=%d; Path=/; Domain=.jogakjogak.com; HttpOnly; SameSite=Lax; Secure",
                cookieName,
                cookieValue,
                60 * 60 * 24 * 7
            );
        }
  
        response.addHeader("Set-Cookie", cookieHeader);
    }


    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 24 * 7);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

    @Transactional
    private void addRefreshToken(String userName, String refresh) {
        refreshTokenRepository.findByUsername(userName)
                .ifPresent(refreshTokenRepository::delete);
        RefreshToken refreshToken = RefreshToken.builder()
                .username(userName)
                .token(refresh)
                .expiration(LocalDateTime.now().plusSeconds(REFRESH_TOKEN_EXPIRATION / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);
    }
}
