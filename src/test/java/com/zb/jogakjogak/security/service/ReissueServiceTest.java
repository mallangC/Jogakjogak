package com.zb.jogakjogak.security.service;

import com.zb.jogakjogak.security.Token;
import com.zb.jogakjogak.security.dto.ReissueResultDto;
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
        reissueService = new ReissueService(jwtUtil, refreshTokenRepository, memberRepository);
    }

    @Test
    @DisplayName("기존 refresh 토큰이 존재할 경우 업데이트")
    void reissue_when_exists_refresh_token_test() throws Exception{
        // given
        String refreshToken = faker.internet().uuid();
        String username = faker.name().username();
        String provider = faker.options().option("kakao", "google");
        String role = "USER";
        String newAccess = faker.internet().uuid();
        String newRefresh = faker.internet().uuid();

        RefreshToken existingToken = RefreshToken.builder()
                .username(username)
                .token(refreshToken)
                .expiration(LocalDateTime.now().plusDays(7))
                .build();

        when(jwtUtil.getUsername(refreshToken)).thenReturn(username);
        when(jwtUtil.getRole(refreshToken)).thenReturn(role);
        when(jwtUtil.createAccessToken(anyLong(), eq(role), eq(provider), eq(username), anyLong(), eq(Token.ACCESS_TOKEN))).thenReturn(newAccess);
        when(jwtUtil.createRefreshToken(anyLong(), anyLong(), eq(Token.REFRESH_TOKEN))).thenReturn(newRefresh);
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.ofNullable(existingToken));

        // when
        ReissueResultDto result = reissueService.reissue(refreshToken);

        // then
        assertThat(result.getNewAccessToken()).isEqualTo(newAccess);
        assertThat(result.getNewRefreshToken()).isEqualTo(newRefresh);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("기존 refresh 토큰이 없을경우 생성후 DB저장")
    void reissue_when_not_exists_refresh_token_test() throws Exception{
        // given
        String refreshToken = faker.internet().uuid();
        String username = faker.name().username();
        String provider = faker.options().option("kakao", "google");
        String role = "USER";
        String newAccess = faker.internet().uuid();
        String newRefresh = faker.internet().uuid();

        when(jwtUtil.getUsername(refreshToken)).thenReturn(username);
        when(jwtUtil.getRole(refreshToken)).thenReturn(role);
        when(jwtUtil.createAccessToken(anyLong(), eq(role), eq(provider), eq(username), anyLong(), eq(Token.ACCESS_TOKEN))).thenReturn(newAccess);
        when(jwtUtil.createRefreshToken(anyLong(), anyLong(), eq(Token.REFRESH_TOKEN))).thenReturn(newRefresh);
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        // when
        ReissueResultDto result = reissueService.reissue(refreshToken);

        // then
        assertThat(result.getNewAccessToken()).isEqualTo(newAccess);
        assertThat(result.getNewRefreshToken()).isEqualTo(newRefresh);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }
}