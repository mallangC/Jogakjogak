package com.zb.jogakjogak.security.repository;


import com.zb.jogakjogak.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    void deleteByUserId(Long userId);
    void deleteByUsername(String username);
    Optional<RefreshToken> findByToken(String refreshToken);

    Optional<RefreshToken> findByUsername(String userName);
}
