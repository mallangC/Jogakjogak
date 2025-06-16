package com.zb.jogakjogak.jobDescription.repository;

import com.zb.jogakjogak.jobDescription.entity.JD;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface JDRepository extends JpaRepository<JD, Long> {
    @Query("SELECT jd FROM JD jd JOIN FETCH jd.toDoLists WHERE jd.id = :id")
    Optional<JD> findByIdWithToDoLists(@Param("id") Long id);

    @Query("SELECT jd FROM JD jd WHERE jd.updatedAt <= :oldDate")
    Page<JD> findOutdatedJD(@Param("oldDate")LocalDateTime oldDate, Pageable pageable);
}
