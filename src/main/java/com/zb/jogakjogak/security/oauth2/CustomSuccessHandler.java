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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GaMeasurementProtocolService gaService;
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;
    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        String username = customOAuth2User.getName();
        String role = getRole(authentication);
        String refreshToken = jwtUtil.createJwt(username, role, REFRESH_TOKEN_EXPIRATION, Token.REFRESH_TOKEN);

        addRefreshToken(username, refreshToken);

        addSameSiteCookieAttribute(request, response, "refresh", refreshToken);

        Member member = customOAuth2User.getMember();
        String clientId = extractGaClientId(request);
        String gaUserId = member.getId().toString();
        String eventName = "user_login";

        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put("login_method", getLoginMethod(authentication));
        eventParams.put("user_role", role);
        gaService.sendGaEvent(clientId, gaUserId, eventName, eventParams)
                .subscribe();

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.println("""
                    <html>
                      <head><meta charset='UTF-8'></head>
                      <body>
                        <script>
                          window.location.href = 'https://jogakjogak.com';
                        </script>
                      </body>
                    </html>
                """);
        writer.flush();
    }

    private String getRole(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        return auth.getAuthority();
    }

    private void addSameSiteCookieAttribute(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue) {
        String serverName = request.getServerName();

        boolean isLocal = serverName.contains("localhost");

        String cookieHeader = String.format(
                "%s=%s; Max-Age=%d; Path=/; HttpOnly; SameSite=None%s",
                cookieName,
                cookieValue,
                60 * 60 * 24 * 7,
                isLocal ? "" : "; Secure"
        );
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

    /**
     * Authentication 객체에서 로그인 방식을 추출합니다.
     * CustomOAuth2User에 포함된 provider 정보를 직접 사용하여 구체적인 제공자 이름을 반환합니다.
     *
     * @param authentication Authentication 객체
     * @return 로그인 방식 (예: "kakao_oauth", "google_oauth", "form_login", "unknown_login_method")
     */
    private String getLoginMethod(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomOAuth2User) {
            CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            return customOAuth2User.getProvider() + "_oauth";
        }
        return "unknown_login_method";
    }
}
