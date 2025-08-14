package com.zb.jogakjogak.security.jwt;

import com.zb.jogakjogak.global.exception.AuthException;
import com.zb.jogakjogak.global.exception.MemberErrorCode;
import com.zb.jogakjogak.security.Token;
import com.zb.jogakjogak.security.repository.RefreshTokenRepository;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
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

    public String getUserId(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public String getProvider(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("provider", String.class);
    }

    public String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
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

    public String createAccessToken(Long userId, String provider, String username, String role, Long expireMs, Token token){
        return Jwts.builder()
                .claims()
                    .subject(String.valueOf(userId))
                    .add("provider", provider)
                    .add("username", username)
                    .add("role", role)
                    .add("token", token.name())
                    .and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(secretKey)
                .compact();
    }
    public String createRefreshToken(Long userId, Long expireMs, Token token){
        return Jwts.builder()
                .claims()
                    .subject(String.valueOf(userId))
                    .add("token", token.name())
                    .and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expireMs))
                .signWith(secretKey)
                .compact();
    }

    public void validateToken(String token, Token tokenType) {
        if (token == null) {
            throw new AuthException(MemberErrorCode.NOT_FOUND_TOKEN);
        }
        try {
            if (isExpired(token)) {
                throw new AuthException(MemberErrorCode.TOKEN_EXPIRED);
            }

            if (!getToken(token).equals(tokenType.name())) {
                throw new AuthException(MemberErrorCode.TOKEN_TYPE_NOT_MATCH);
            }
        } catch (JwtException | IllegalArgumentException e){
            throw new AuthException(MemberErrorCode.INVALID_ACCESS_TOKEN);
        }
    }
}
