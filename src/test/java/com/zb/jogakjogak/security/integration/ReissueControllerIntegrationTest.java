package com.zb.jogakjogak.security.integration;


import com.zb.jogakjogak.security.Token;
import com.zb.jogakjogak.security.entity.RefreshToken;
import com.zb.jogakjogak.security.jwt.JWTUtil;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ReissueControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final Faker faker = new Faker();

    private String refreshToken;
    private String userName = "testUser";
    private String role = "USER";

    @BeforeEach
    void setup() {
        // 기존 refresh token 생성 및 DB 저장

        refreshToken = jwtUtil.createJwt(userName, role, 7 * 24 * 60 * 60 * 1000L, Token.REFRESH_TOKEN);

        RefreshToken entity = RefreshToken.builder()
                .userName(userName)
                .refreshToken(refreshToken)
                .expiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L).toString())
                .build();

        refreshTokenRepository.save(entity);
    }

    @Test
    @DisplayName("통합 테스트 - refresh 토큰이 유효하면 access + refresh 재발급 성공")
    void reissue_integration_success_test() throws Exception {
        //given
        String userName = "testUser2";
        String newRefreshToken = jwtUtil.createJwt(userName, role, 7 * 24 * 60 * 60 * 1000L, Token.REFRESH_TOKEN);

        // when
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/api/member/reissue")
                        .with(csrf())
                        .with(user("testUser"))
                        .cookie(new Cookie("refresh", newRefreshToken)))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        // access 토큰이 응답 헤더에 존재
        String newAccess = response.getHeader("Authorization");
        assertThat(newAccess).isNotNull();
        assertThat(newAccess).startsWith("Bearer ");

        // refresh 토큰이 새로 발급되어 쿠키에 포함
        Cookie[] cookies = response.getCookies();
        boolean hasNewRefresh = false;
        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName()) && !cookie.getValue().equals(refreshToken)) {
                hasNewRefresh = true;
                break;
            }
        }
        assertThat(hasNewRefresh).isTrue();
    }
}
