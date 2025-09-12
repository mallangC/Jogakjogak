package com.zb.jogakjogak.jobDescription.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zb.jogakjogak.jobDescription.entity.JD;
import com.zb.jogakjogak.jobDescription.entity.QJD;
import com.zb.jogakjogak.jobDescription.entity.QToDoList;
import com.zb.jogakjogak.resume.entity.QResume;
import com.zb.jogakjogak.security.entity.QMember;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JDRepositoryImpl implements JDRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public JDRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Optional<JD> findJdWithMemberAndToDoListsByIdAndMemberId(Long jdId, Long memberId) {
        QJD jd = QJD.jD;
        QMember member = QMember.member;
        QToDoList toDoList = QToDoList.toDoList;
        QResume resume = QResume.resume;

        JD foundJd = queryFactory
                .selectFrom(jd)
                .join(jd.member, member).fetchJoin()
                .leftJoin(jd.toDoLists, toDoList).fetchJoin()
                .join(member.resume, resume).fetchJoin()
                .where(jd.id.eq(jdId)
                        .and(member.id.eq(memberId)))
                .fetchOne();

        return Optional.ofNullable(foundJd);
    }

    @Override
    public Page<JD> findAllJdsByMemberIdWithToDoLists(Long memberId,
                                                      Pageable pageable,
                                                      String showOnly ) {
        QJD jd = QJD.jD;
        QToDoList toDoList = QToDoList.toDoList;

        BooleanBuilder whereBuilder = new BooleanBuilder();
        whereBuilder.and(jd.member.id.eq(memberId));

        switch (showOnly) {
            case "bookmark": //즐겨 찾기 된 jd
                whereBuilder.and(jd.isBookmark.isTrue());
                break;
            case "completed": // 완료된 jd
                whereBuilder.and(jd.applyAt.isNotNull());
                break;
            case "alarm": // 알람 on 설정된 jd
                whereBuilder.and(jd.isAlarmOn.isTrue());
                break;
            // "normal"이거나 알 수 없는 값인 경우 추가 필터링 없음
            default:
                break;
        }

        // 쿼리 결과 리스트 조회
        List<JD> content = queryFactory
                .selectFrom(jd)
                .leftJoin(jd.toDoLists, toDoList).fetchJoin()
                .where(whereBuilder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort(), jd).toArray(OrderSpecifier[]::new))
                .fetch();

        // 전체 카운트 조회 (페이징을 위해 필요)
        Long total = queryFactory
                .select(jd.count())
                .from(jd)
                .where(whereBuilder)
                .fetchOne();

        // Page 객체 생성 및 반환
        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    @Override
    public Page<JD> findNotUpdatedJdByQueryDsl(LocalDateTime now, LocalDateTime todayStart, Pageable pageable) {
        QJD jd = QJD.jD;
        QMember member = QMember.member;

        LocalDateTime threeDaysAgoDate = LocalDate.now().atStartOfDay().minusDays(3);

        List<JD> content = queryFactory
                .selectFrom(jd)
                .join(jd.member, member).fetchJoin()
                .where(
                        jd.updatedAt.loe(threeDaysAgoDate),
                        jd.isAlarmOn.isTrue(),
                        jd.notificationCount.lt(3),
                        jd.lastNotifiedAt.isNull().or(jd.lastNotifiedAt.lt(todayStart)),
                        jd.endedAt.isNull().or(jd.endedAt.goe(now))
                        )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort(), jd).toArray(OrderSpecifier[]::new))
                .fetch();

        Long total = queryFactory
                .select(jd.count())
                .from(jd)
                .where(
                        jd.updatedAt.loe(threeDaysAgoDate),
                        jd.isAlarmOn.isTrue(),
                        jd.notificationCount.lt(3),
                        jd.lastNotifiedAt.isNull().or(jd.lastNotifiedAt.lt(todayStart)),
                        jd.endedAt.isNull().or(jd.endedAt.goe(now))
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }


    @Override
    public Long findAllJdCountByMemberId(Long memberId) {
        QJD jd = QJD.jD;
        return queryFactory.select(jd.count())
                .from(jd)
                .where(jd.member.id.eq(memberId))
                .fetchOne();
    }


    /**
     * Pageable의 Sort 정보를 QueryDSL의 OrderSpecifier 리스트로 변환합니다.
     *
     * @param sort Pageable에서 추출한 Sort 객체
     * @param qjd  QJD 엔티티 경로
     * @return QueryDSL OrderSpecifier 리스트
     */
    private List<OrderSpecifier<?>> getOrderSpecifiers(Sort sort, QJD qjd) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        for (Sort.Order order : sort) {
            switch (order.getProperty()) {
                case "createdAt": // 최신순, 오래된순
                    orderSpecifiers.add(order.isAscending() ? qjd.createdAt.asc() : qjd.createdAt.desc());
                    break;
                case "endedAt": // 마감일 순
                    orderSpecifiers.add(order.isAscending() ? qjd.endedAt.asc() : qjd.endedAt.desc());
                    break;
                case "applyAt": // 완료 순
                    orderSpecifiers.add(order.isAscending() ? qjd.applyAt.asc() : qjd.applyAt.desc());
                    break;
                case "isBookmark": // 즐겨찾기 순 (true가 먼저 오도록)
                    orderSpecifiers.add(order.isAscending() ? qjd.isBookmark.asc() : qjd.isBookmark.desc());
                    break;
                case "isAlarmOn": // 알림 순 (true가 먼저 오도록)
                    orderSpecifiers.add(order.isAscending() ? qjd.isAlarmOn.asc() : qjd.isAlarmOn.desc());
                    break;
                default:
                    break;
            }
        }
        if (orderSpecifiers.isEmpty()) {
            orderSpecifiers.add(qjd.createdAt.desc());
        }
        return orderSpecifiers;
    }
}
