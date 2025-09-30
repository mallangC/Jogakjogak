package com.zb.jogakjogak.event.repository;

import com.zb.jogakjogak.event.entity.Event;
import com.zb.jogakjogak.event.type.EventType;

import java.util.Optional;

public interface EventRepositoryCustom {
    Optional<Event> findByMemberIdAndType(Long memberId, EventType type);
}
