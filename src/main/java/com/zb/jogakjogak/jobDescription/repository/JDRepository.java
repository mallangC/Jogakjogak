package com.zb.jogakjogak.jobDescription.repository;

import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.security.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JDRepository extends JpaRepository<JD, Long>, JDRepositoryCustom {

    //테스트용
    List<JD> findAllByMember(Member member);

}
