package com.zb.jogakjogak.security.service;


import com.zb.jogakjogak.global.exception.CustomException;
import com.zb.jogakjogak.security.Token;
import com.zb.jogakjogak.security.dto.ReissueResultDto;
import com.zb.jogakjogak.security.entity.RefreshToken;
import com.zb.jogakjogak.security.jwt.JWTUtil;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import com.zb.jogakjogak.global.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ReissueService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public ReissueResultDto reissue(String refreshToken) {

        if (refreshToken == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_TOKEN);
        }
        // 유효기간 체크
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        }
        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        if (!jwtUtil.getToken(refreshToken).equals(Token.REFRESH_TOKEN.name())) {
            System.out.println("리프레쉬 토큰이 아닙니다.");
            return null; // 또는 throw new CustomException(...)

            //throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // DB에 저장되어 있는지 확인
        if (!refreshTokenRepository.existsByRefreshToken(refreshToken)) {
            System.out.println("존재하지 않는 토큰입니다.");
            return null; // 또는 throw new CustomException(...)

            //throw new CustomException(ErrorCode.TOKEN_NOT_FOUND);
        }

        String userName = jwtUtil.getUserName(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        // 새로운 jwt발급
        String newAccess = jwtUtil.createJwt(userName, role, 600000L, Token.ACCESS_TOKEN);
        String newRefresh = jwtUtil.createJwt(userName, role, 86400000L, Token.REFRESH_TOKEN);

        // refresh 토큰 저장 db에 기존의 refresh토큰 삭제후 새 refresh토큰 저장
        RefreshToken existingToken = refreshTokenRepository.findByRefreshToken(refreshToken);
        if (existingToken != null) {
            // 기존 엔티티 업데이트
            existingToken.setRefreshToken(newRefresh);
            existingToken.setExpiration(new Date(System.currentTimeMillis() + 86400000L).toString());
            refreshTokenRepository.save(existingToken);
        } else {
            // 새로 생성
            saveNewRefreshToken(userName, newRefresh, 86400000L);
        }

        return ReissueResultDto.builder()
                .newAccessToken(newAccess)
                .newRefreshToken(newRefresh)
                .build();
    }


    private void saveNewRefreshToken(String userName, String newRefresh, Long expiredMs) {
        Date date = new Date(System.currentTimeMillis() + expiredMs);
        RefreshToken refreshToken = RefreshToken.builder()
                .userName(userName)
                .refreshToken(newRefresh)
                .expiration(date.toString())
                .build();
        refreshTokenRepository.save(refreshToken);
    }
}
