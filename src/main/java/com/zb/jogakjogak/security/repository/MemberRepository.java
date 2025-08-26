package com.zb.jogakjogak.security.repository;

import com.zb.jogakjogak.security.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);

    @Query("SELECT COUNT(j) FROM JD j WHERE j.member.id = :memberId")
    long countJdByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.oauth2Info WHERE m.username = :username")
    Optional<Member> findByUsernameWithOauth2Info(@Param("username") String username);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.resume WHERE m.username = :username")
    Optional<Member> findByUsernameWithResume(@Param("username") String username);

    boolean existsByNickname(String nickname);

    List<Member> findByNicknameIsNull();
}
