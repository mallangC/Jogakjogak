package com.zb.jogakjogak.notification.repository;

import com.zb.jogakjogak.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByMemberId(Long memberId);
}
