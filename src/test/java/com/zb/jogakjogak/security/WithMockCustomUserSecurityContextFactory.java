package com.zb.jogakjogak.security;

import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.entity.OAuth2Info;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        String testUsername = annotation.username();
        String testRealName = annotation.realName();
        String testEmail = annotation.email();
        String testRole = annotation.role();
        List<OAuth2Info> oAuth2Infos = Collections.singletonList(OAuth2Info.builder()
                .provider("kakao")
                .providerId("4307188795")
                .build());

        Member mockMember = Member.builder()
                .id(1L)
                .username(testUsername)
                .name(testRealName)
                .email(testEmail)
                .role(Role.valueOf(testRole))
                .oauth2Info(oAuth2Infos)
                .password("mock_password")
                .phoneNumber("01012345678")
                .nickname("mock_nickname")
                .lastLoginAt(LocalDateTime.now())
                .build();

        CustomOAuth2User principal = new CustomOAuth2User(mockMember);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                "password",
                principal.getAuthorities()
        );
        context.setAuthentication(auth);
        return context;
    }
}
