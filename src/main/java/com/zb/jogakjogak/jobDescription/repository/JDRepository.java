package com.zb.jogakjogak.jobDescription.repository;

import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.security.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JDRepository extends JpaRepository<JD, Long>, JDRepositoryCustom {

    List<JD> findAllByMember(Member member);

    /** 배치 테스트용
     *
     * @param pageable
     * @return
     */
    @Query("SELECT j FROM JD j JOIN FETCH j.member WHERE j.notificationCount <= 3")
    Page<JD> findAllJdsWithMember(Pageable pageable);

}
