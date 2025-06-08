package com.zb.jogakjogak.security.jwt;

import com.zb.jogakjogak.global.exception.CustomException;
import com.zb.jogakjogak.global.exception.ErrorCode;
import com.zb.jogakjogak.security.Token;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {

    private final SecretKey secretKey;

    public JWTUtil(@Value("${jwt.secret-key}")String secret, RefreshTokenRepository refreshTokenRepository){
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String getUserName(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userName", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public String getToken(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("token", String.class);
    }

    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public String createJwt(String userName, String role, Long expireMs, Token token){
        return Jwts.builder()
                .claim("userName", userName)
                .claim("role", role)
                .claim("token", token.name())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(secretKey)
                .compact();
    }

    public void validationToken(String token){
        if (token == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_TOKEN);
        }
        try {
            isExpired(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        }
        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        if (!getToken(token).equals(Token.REFRESH_TOKEN.name())) {
            throw new CustomException(ErrorCode.NOT_REFRESH_TOKEN);
        }
    }
}
