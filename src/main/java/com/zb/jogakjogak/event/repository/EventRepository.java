package com.zb.jogakjogak.event.repository;

import com.zb.jogakjogak.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {
    boolean findByCode(String code);

    boolean existsByCode(String code);
}
