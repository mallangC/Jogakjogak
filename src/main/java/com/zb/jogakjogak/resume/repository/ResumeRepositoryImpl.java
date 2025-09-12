package com.zb.jogakjogak.resume.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zb.jogakjogak.resume.entity.*;
import com.zb.jogakjogak.security.entity.QMember;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.zb.jogakjogak.resume.entity.QCareer.career;
import static com.zb.jogakjogak.resume.entity.QEducation.education;
import static com.zb.jogakjogak.resume.entity.QResume.resume;
import static com.zb.jogakjogak.resume.entity.QSkill.skill;

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

        Resume findResume = queryFactory.selectFrom(resume)
                .join(resume.careerList, career).fetchJoin()
                .join(resume.educationList, education).fetchJoin()
                .join(resume.skillList, skill).fetchJoin()
                .where(resume.member.id.eq(memberId))
                .fetchOne();

        return Optional.ofNullable(findResume);
    }

    @Transactional
    @Override
    public void deleteAboutResumeById(Long resumeId) {
        queryFactory.delete(career)
                .where(career.resume.id.eq(resumeId))
                .execute();
        queryFactory.delete(education)
                .where(education.resume.id.eq(resumeId))
                .execute();
        queryFactory.delete(skill)
                .where(skill.resume.id.eq(resumeId))
                .execute();
    }
}
