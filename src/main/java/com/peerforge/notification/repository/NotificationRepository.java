package com.peerforge.notification.repository;

import com.peerforge.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop50ByRecipient_IdOrderByCreatedAtDescIdDesc(Long recipientId);

    long countByRecipient_IdAndReadAtIsNull(Long recipientId);

    List<Notification> findByRecipient_IdAndReadAtIsNull(Long recipientId);

    Optional<Notification> findByIdAndRecipient_Id(Long id, Long recipientId);
}
