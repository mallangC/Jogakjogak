package com.zb.jogakjogak.security.controller;

import com.zb.jogakjogak.ga.service.GaMeasurementProtocolService;
import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.service.WithdrawalService;
import jakarta.servlet.http.Cookie;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@WebMvcTest(WithdrawalController.class)
class WithdrawalTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockBean
    private WithdrawalService withdrawalService;

    @MockitoBean
    private GaMeasurementProtocolService gaMeasurementProtocolService;

    private final Faker faker = new Faker();

    @Test
    @DisplayName("OAuth2 사용자 회원탈퇴 성공")
    void oauth2User_withdrawal_test() throws Exception {
        // given
        String username = faker.internet().username();
        String email = faker.internet().emailAddress();

        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getName()).thenReturn(username);
        when(customOAuth2User.getAttributes()).thenReturn(Map.of(
                "email", email,
                "name", username
        ));
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                customOAuth2User,
                customOAuth2User.getAuthorities(),
                "kakao"
        );

        // when
        MockHttpServletResponse response = mockMvc.perform(delete("/member/withdrawal")
                        .with(csrf())
                        .with(authentication(authentication)))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).contains("회원탈퇴 완료");

        // refresh 쿠키가 삭제되었는지 확인
        Cookie[] cookies = response.getCookies();
        boolean isRefreshCookieCleared = false;
        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName()) && cookie.getValue() == null && cookie.getMaxAge() == 0) {
                isRefreshCookieCleared = true;
                break;
            }
        }
        assertThat(isRefreshCookieCleared).isTrue();
        verify(withdrawalService, times(1)).withdrawMember(username);
    }

    private OAuth2AuthenticationToken createMockAuthentication(String username) {
        CustomOAuth2User user = mock(CustomOAuth2User.class);
        when(user.getName()).thenReturn(username);

        return new OAuth2AuthenticationToken(
                user,
                user.getAuthorities(),
                "kakao"
        );
    }
}