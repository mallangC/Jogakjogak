package com.zb.jogakjogak.resume.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zb.jogakjogak.resume.entity.*;
import com.zb.jogakjogak.security.entity.QMember;
import jakarta.persistence.EntityManager;

import java.util.Optional;

public class ResumeRepositoryImpl implements ResumeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ResumeRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Optional<Resume> findResumeWithMemberByIdAndMemberId(Long resumeId, Long memberId) {
        QResume resume = QResume.resume;
        QMember member = QMember.member;

        Resume foundResume = queryFactory
                .selectFrom(resume)
                .join(resume.member, member)
                .fetchJoin()
                .where(resume.id.eq(resumeId)
                        .and(member.id.eq(memberId)))
                .fetchOne();

        return Optional.ofNullable(foundResume);
    }

    @Override
    public Optional<Resume> findResumeWithCareerAndEducationAndSkill(Long memberId) {
        QResume resume = QResume.resume;
        QCareer career = QCareer.career;
        QEducation education = QEducation.education;
        QSkill skill = QSkill.skill;

        Resume findResume = queryFactory.selectFrom(resume)
                .leftJoin(resume.careerList, career).fetchJoin()
                .leftJoin(resume.educationList, education).fetchJoin()
                .leftJoin(resume.skillList, skill).fetchJoin()
                .where(resume.member.id.eq(memberId))
                .fetchOne();

        return Optional.ofNullable(findResume);
    }
}
