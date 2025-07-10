package com.zb.jogakjogak.security.service;

import com.zb.jogakjogak.security.Token;
import com.zb.jogakjogak.security.dto.ReissueResultDto;
import com.zb.jogakjogak.security.entity.Member;
import com.zb.jogakjogak.security.entity.RefreshToken;
import com.zb.jogakjogak.security.jwt.JWTUtil;
import com.zb.jogakjogak.security.repository.MemberRepository;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReissueServiceTest {

    private JWTUtil jwtUtil;
    private RefreshTokenRepository refreshTokenRepository;
    private MemberRepository memberRepository;
    private ReissueService reissueService;
    private final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JWTUtil.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        memberRepository = mock(MemberRepository.class);
        reissueService = new ReissueService(jwtUtil, refreshTokenRepository, memberRepository);
    }

    @Test
    @DisplayName("기존 refresh 토큰이 존재할 경우 업데이트")
    void reissue_when_exists_refresh_token_test() throws Exception{
        // given
        String refreshToken = faker.internet().uuid();
        String username = faker.name().username();
        String provider = "kakao"; // faker.options().option("kakao", "google"); 해도 되지만 고정하는 게 안정적
        String role = "USER";
        String userIdStr = "1"; // 숫자 문자열로 넣기 (Long.parseLong 가능해야 함)
        Long userId = Long.parseLong(userIdStr);
        String newAccess = faker.internet().uuid();
        String newRefresh = faker.internet().uuid();

        RefreshToken existingToken = RefreshToken.builder()
                .username(username)
                .token(refreshToken)
                .expiration(LocalDateTime.now().plusDays(7))
                .build();

        // Member mock
        Member member = mock(Member.class);
        // OAuth2Info 리스트 mock
        var oauth2Info = mock(java.util.List.class);
        var firstOauth2 = mock(com.zb.jogakjogak.security.entity.OAuth2Info.class);

        when(member.getOauth2Info()).thenReturn(java.util.List.of(firstOauth2));
        when(firstOauth2.getProvider()).thenReturn(provider);
        when(member.getUsername()).thenReturn(username);

        when(jwtUtil.getUserId(refreshToken)).thenReturn(userIdStr);
        when(jwtUtil.getRole(refreshToken)).thenReturn(role);
        when(jwtUtil.createAccessToken(userId, provider, username, role, 1000 * 60 * 60 * 24L, Token.ACCESS_TOKEN)).thenReturn(newAccess);
        when(jwtUtil.createRefreshToken(userId, 7 * 24 * 60 * 60 * 1000L, Token.REFRESH_TOKEN)).thenReturn(newRefresh);
        when(refreshTokenRepository.findByUsername(username)).thenReturn(Optional.of(existingToken));
        when(memberRepository.findById(userId)).thenReturn(Optional.of(member));

        // when
        ReissueResultDto result = reissueService.reissue(refreshToken);

        // then
        assertThat(result.getNewAccessToken()).isEqualTo(newAccess);
        assertThat(result.getNewRefreshToken()).isEqualTo(newRefresh);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }


    @Test
    @DisplayName("기존 refresh 토큰이 없을경우 생성후 DB저장")
    void reissue_when_not_exists_refresh_token_test() throws Exception {
        // given
        String refreshToken = faker.internet().uuid();
        String username = faker.name().username();
        String provider = "kakao"; // 안정적으로 고정
        String role = "USER";
        String userIdStr = "1";
        Long userId = Long.parseLong(userIdStr);
        String newAccess = faker.internet().uuid();
        String newRefresh = faker.internet().uuid();

        // Member mock 생성 및 세팅
        Member member = mock(Member.class);
        var firstOauth2 = mock(com.zb.jogakjogak.security.entity.OAuth2Info.class);

        when(member.getOauth2Info()).thenReturn(java.util.List.of(firstOauth2));
        when(firstOauth2.getProvider()).thenReturn(provider);
        when(member.getUsername()).thenReturn(username);

        // jwtUtil mock
        when(jwtUtil.getUserId(refreshToken)).thenReturn(userIdStr);
        when(jwtUtil.getRole(refreshToken)).thenReturn(role);
        when(jwtUtil.createAccessToken(userId, provider, username, role, 1000 * 60 * 60 * 24L, Token.ACCESS_TOKEN)).thenReturn(newAccess);
        when(jwtUtil.createRefreshToken(userId, 7 * 24 * 60 * 60 * 1000L, Token.REFRESH_TOKEN)).thenReturn(newRefresh);

        // DB mock
        when(refreshTokenRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(memberRepository.findById(userId)).thenReturn(Optional.of(member));

        // when
        ReissueResultDto result = reissueService.reissue(refreshToken);

        // then
        assertThat(result.getNewAccessToken()).isEqualTo(newAccess);
        assertThat(result.getNewRefreshToken()).isEqualTo(newRefresh);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }
}