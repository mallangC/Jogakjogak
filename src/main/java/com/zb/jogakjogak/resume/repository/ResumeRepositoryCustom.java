package com.zb.jogakjogak.resume.repository;

import com.zb.jogakjogak.resume.entity.Resume;

import java.util.Optional;

public interface ResumeRepositoryCustom {

    /**
     * 이력서 ID와 사용자 ID를 기반으로 이력서와 연관된 Member를 즉시 로딩하여 조회합니다.
     * N+1 문제를 방지하기 위해 fetch join을 사용합니다.
     *
     * @param resumeId 조회할 이력서의 ID
     * @param memberId 이력서 소유자의 ID
     * @return 조회된 Resume 객체
     */
    Optional<Resume> findResumeWithMemberByIdAndMemberId(Long resumeId, Long memberId);
}
