package com.zb.jogakjogak.security.repository;

import com.zb.jogakjogak.security.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByUserName(String userName);
    boolean existsByUserName(String userName);
}
