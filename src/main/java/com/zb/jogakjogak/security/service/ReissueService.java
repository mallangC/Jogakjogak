package com.zb.jogakjogak.security.service;


import com.zb.jogakjogak.security.Token;
import com.zb.jogakjogak.security.dto.ReissueResultDto;
import com.zb.jogakjogak.security.entity.RefreshToken;
import com.zb.jogakjogak.security.jwt.JWTUtil;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReissueService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24L;
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L;

    public ReissueResultDto reissue(String refreshToken) {

        jwtUtil.validateToken(refreshToken, Token.REFRESH_TOKEN);

        String userName = jwtUtil.getUserName(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        String newAccess = jwtUtil.createJwt(userName, role, ACCESS_TOKEN_EXPIRATION, Token.ACCESS_TOKEN);
        String newRefresh = jwtUtil.createJwt(userName, role, REFRESH_TOKEN_EXPIRATION, Token.REFRESH_TOKEN);

        // refresh 토큰 저장 DB에 존재하면 업데이트, 없으면 생성
        Optional<RefreshToken> existingToken = refreshTokenRepository.findByToken(refreshToken);
        if (existingToken.isPresent()) {
            updateExistingRefreshTokenEntity(existingToken.get(), newRefresh);
        } else {
            saveNewRefreshTokenEntity(userName, newRefresh);
        }

        return ReissueResultDto.builder()
                .newAccessToken(newAccess)
                .newRefreshToken(newRefresh)
                .build();
    }

    private void updateExistingRefreshTokenEntity(RefreshToken existingToken, String newRefresh) {
        existingToken.setToken(newRefresh);
        existingToken.setExpiration(LocalDateTime.now().plusSeconds(REFRESH_TOKEN_EXPIRATION / 1000));
        refreshTokenRepository.save(existingToken);
    }

    private void saveNewRefreshTokenEntity(String userName, String newRefresh) {
        RefreshToken refreshToken = RefreshToken.builder()
                .username(userName)
                .token(newRefresh)
                .expiration(LocalDateTime.now().plusSeconds(REFRESH_TOKEN_EXPIRATION / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);
    }
}
