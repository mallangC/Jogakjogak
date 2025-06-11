package com.zb.jogakjogak.security.repository;


import com.zb.jogakjogak.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByRefreshToken(String refresh);
    RefreshToken findByRefreshToken(String refreshToken);
}
