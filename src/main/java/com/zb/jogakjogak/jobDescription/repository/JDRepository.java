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

    //테스트용
    @EntityGraph(attributePaths = "member")
    Page<JD> findAll(Pageable pageable);
}
