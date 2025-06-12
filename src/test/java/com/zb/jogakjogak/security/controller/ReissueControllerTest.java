package com.zb.jogakjogak.security.controller;

import com.zb.jogakjogak.security.dto.ReissueResultDto;
import com.zb.jogakjogak.security.service.ReissueService;
import jakarta.servlet.http.Cookie;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(ReissueController.class)
class ReissueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private ReissueService reissueService;

    private final Faker faker = new Faker();

    @Test
    @DisplayName("refresh, access토큰 재발급 성공")
    void refreshToken_reissue_success_test() throws Exception {
        // given
        String oldRefresh = faker.internet().uuid();
        String newAccess = faker.internet().uuid();
        String newRefresh = faker.internet().uuid();

        ReissueResultDto mockResult = ReissueResultDto.builder()
                .newAccessToken(newAccess)
                .newRefreshToken(newRefresh)
                .build();

        when(reissueService.reissue(oldRefresh)).thenReturn(mockResult);

        // when
        MockHttpServletResponse response = mockMvc.perform(post("/api/member/reissue")
                        .with(csrf())
                        .with(user("testUser"))
                        .cookie(new Cookie("refresh", oldRefresh)))
                .andReturn()
                .getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getHeader("Authorization")).isEqualTo("Bearer " + newAccess);

        Cookie[] cookies = response.getCookies();
        boolean hasRefreshCookie = false;
        for (Cookie cookie : cookies) {
            if ("refresh".equals(cookie.getName()) && newRefresh.equals(cookie.getValue())) {
                hasRefreshCookie = true;
                break;
            }
        }
        assertThat(hasRefreshCookie).isTrue();
    }
}