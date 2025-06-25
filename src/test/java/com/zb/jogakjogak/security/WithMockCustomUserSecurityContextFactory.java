package com.zb.jogakjogak.security;

import com.zb.jogakjogak.security.dto.CustomOAuth2User;
import com.zb.jogakjogak.security.entity.Member;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.time.LocalDateTime;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        String testUsername = annotation.username();
        String testRealName = annotation.realName();
        String testEmail = annotation.email();
        String testRole = annotation.role();

        Member mockMember = Member.builder()
                .username(testUsername)
                .name(testRealName)
                .email(testEmail)
                .role(Role.valueOf(testRole))
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
