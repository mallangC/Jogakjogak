package com.zb.jogakjogak.security.repository;


import com.zb.jogakjogak.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByToken(String refresh);
    Optional<RefreshToken> findByToken(String refreshToken);

    void deleteByUsername(String username);
}
