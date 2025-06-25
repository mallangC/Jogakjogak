package com.zb.jogakjogak.jobDescription.repository;

import com.zb.jogakjogak.jobDescription.entity.JD;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface JDRepository extends JpaRepository<JD, Long> {
    /**
     * 주어진 ID에 해당하는 JD (Job Description) 엔티티를 조회하면서,
     * 연관된 'toDoLists' 컬렉션을 즉시 로딩(Eager Loading)하여 가져옵니다.
     *
     * @param id 조회할 JD 엔티티의 고유 ID.
     * @return ID에 해당하는 JD 엔티티와 연관된 ToDoList를 포함하는 Optional<JD>.
     */
    @Query("SELECT jd FROM JD jd JOIN FETCH jd.toDoLists WHERE jd.id = :id")
    Optional<JD> findByIdWithToDoLists(@Param("id") Long id);

    /**
     * 주어진 회원 ID(memberId)에 해당하는 JD (Job Description) 엔티티 목록을 페이징하여 조회합니다.
     * 조회 시 @EntityGraph를 사용하여 'toDoLists' 컬렉션을 즉시 로딩(Eager Loading)합니다.
     *
     * @param memberId 조회할 회원의 고유 ID.
     * @param pageable 페이징 및 정렬 정보를 담는 객체.
     * @return 회원 ID에 해당하는 JD 엔티티와 연관된 ToDoList를 포함하는 age<JD>.
     */
    @EntityGraph(attributePaths = "toDoLists")
    Page<JD> findByMemberId(Long memberId, Pageable pageable);


    @Query("SELECT jd FROM JD jd WHERE jd.updatedAt <= :oldDate AND jd.endedAt >= now AND jd.isAlarmOn = true")
    Page<JD> findNotUpdatedJd(@Param("oldDate")LocalDateTime oldDate, @Param("now") LocalDateTime now, Pageable pageable);

}
