package com.zb.jogakjogak.jobDescription.repository;

import com.zb.jogakjogak.jobDescription.entity.JD;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JDRepositoryCustom {

    /**
     * JD ID와 Member ID를 기반으로 JD, Member, ToDoList를 즉시 로딩하여 조회합니다.
     * 이 메서드는 JD의 소유권 검증을 쿼리 내에서 수행하며, N+1 문제를 방지합니다.
     *
     * @param jdId     조회할 JD의 ID
     * @param memberId JD 소유자의 ID
     * @return 조회된 JD 객체 (Optional)
     */
    Optional<JD> findJdWithMemberAndToDoListsByIdAndMemberId(Long jdId, Long memberId);

    /**
     * 특정 Member의 모든 JD 목록을 페이징하여 조회합니다.
     * 각 JD와 연관된 ToDoList를 즉시 로딩하여 N+1 문제를 방지합니다.
     *
     * @param memberId 조회할 Member의 ID
     * @param pageable 페이징 및 정렬 정보
     * @return 페이징된 JD 목록
     */
    Page<JD> findAllJdsByMemberIdWithToDoLists(Long memberId, Pageable pageable);

    /**
     * 특정 날짜 이전에 업데이트되지 않았고, 마감일이 현재 시간 이후이며, 알림이 켜져 있는 JD 목록을 페이징하여 조회합니다.
     *
     * @param oldDate  업데이트 기준 날짜
     * @param now      현재 시간 (마감일 비교 기준)
     * @param pageable 페이징 및 정렬 정보
     * @return 조건에 맞는 JD 목록
     */
    Page<JD> findNotUpdatedJdByQueryDsl(LocalDateTime oldDate, LocalDateTime now, LocalDateTime todayStart, Pageable pageable);
}
