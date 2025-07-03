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


    /** 테스트용
     *
     * @param pageable
     * @return
     */
    @Query("SELECT j FROM JD j JOIN FETCH j.member")
    Page<JD> findAllJdsWithMember(Pageable pageable);


    @Query("""
        SELECT jd
        FROM JD jd
        WHERE jd.isAlarmOn = true
          AND jd.endedAt >= :today
          AND jd.notificationCount < 3
          AND (jd.lastNotifiedAt IS NULL OR jd.lastNotifiedAt < :todayStart)
          AND jd.updatedAt <= :threeDaysAgo
          ORDER BY jd.endedAt ASC
        """)
    List<JD> findJdToNotify(@Param("today") LocalDateTime today,
                            @Param("threeDaysAgo") LocalDateTime threeDaysAgo,
                            @Param("todayStart") LocalDateTime todayStart);
}
