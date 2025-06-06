package com.zb.jogakjogak.security.service;


import com.zb.jogakjogak.security.Token;
import com.zb.jogakjogak.security.dto.ReissueResultDto;
import com.zb.jogakjogak.security.entity.RefreshToken;
import com.zb.jogakjogak.security.jwt.JWTUtil;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ReissueService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private static final long ACCESS_TOKEN_MS = 3600000L;
    private static final long REFRESH_TOKEN_MS = 604800000L;

    public ReissueResultDto reissue(String refreshToken) {

        jwtUtil.validationToken(refreshToken);

        String userName = jwtUtil.getUserName(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        // 새로운 jwt발급
        String newAccess = jwtUtil.createJwt(userName, role, ACCESS_TOKEN_MS, Token.ACCESS_TOKEN);
        String newRefresh = jwtUtil.createJwt(userName, role, REFRESH_TOKEN_MS, Token.REFRESH_TOKEN);

        // refresh 토큰 저장 DB에 존재하면 업데이트, 없으면 생성
        RefreshToken existingToken = refreshTokenRepository.findByRefreshToken(refreshToken);
        if (existingToken != null) {
            updateExistingRefreshTokenEntity(existingToken, newRefresh);
        } else {
            saveNewRefreshTokenEntity(userName, newRefresh);
        }

        return ReissueResultDto.builder()
                .newAccessToken(newAccess)
                .newRefreshToken(newRefresh)
                .build();
    }

    private void updateExistingRefreshTokenEntity(RefreshToken existingToken, String newRefresh) {
        existingToken.setRefreshToken(newRefresh);
        existingToken.setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_MS).toString());
        refreshTokenRepository.save(existingToken);
    }

    private void saveNewRefreshTokenEntity(String userName, String newRefresh) {
        Date date = new Date(System.currentTimeMillis() + REFRESH_TOKEN_MS);
        RefreshToken refreshToken = RefreshToken.builder()
                .userName(userName)
                .refreshToken(newRefresh)
                .expiration(date.toString())
                .build();
        refreshTokenRepository.save(refreshToken);
    }
}
