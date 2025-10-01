package com.zb.jogakjogak.event.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zb.jogakjogak.event.entity.Event;
import com.zb.jogakjogak.event.type.EventType;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static com.zb.jogakjogak.event.entity.QEvent.event;

@RequiredArgsConstructor
public class EventRepositoryCustomImpl implements EventRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Event> findByMemberIdAndType(Long memberId, EventType type) {
        Event findEvent = queryFactory.selectFrom(event)
                .where(event.member.id.eq(memberId)
                        .and(event.type.eq(type)))
                .fetchOne();

        return Optional.ofNullable(findEvent);
    }
}
