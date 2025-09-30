package com.zb.jogakjogak.event.repository;

import com.zb.jogakjogak.event.entity.Event;
import com.zb.jogakjogak.event.type.EventType;
import com.zb.jogakjogak.security.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface EventRepositoryCustom {
    Optional<Event> findByMemberIdAndType(Long memberId, EventType type);
}
