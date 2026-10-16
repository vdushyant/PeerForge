package com.peerforge.notification.service.impl;

import com.peerforge.common.exception.ResourceNotFoundException;
import com.peerforge.notification.dto.NotificationInboxResponse;
import com.peerforge.notification.dto.NotificationResponse;
import com.peerforge.notification.entity.Notification;
import com.peerforge.notification.entity.NotificationType;
import com.peerforge.notification.repository.NotificationRepository;
import com.peerforge.notification.service.NotificationService;
import com.peerforge.user.entity.User;
import com.peerforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public void notifyUser(User recipient, NotificationType type, String title, String message, String actionUrl) {
        notificationRepository.save(Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationInboxResponse getInbox(String email) {
        User recipient = getUser(email);
        return new NotificationInboxResponse(
                notificationRepository.findTop50ByRecipient_IdOrderByCreatedAtDescIdDesc(recipient.getId())
                        .stream().map(this::toResponse).toList(),
                notificationRepository.countByRecipient_IdAndReadAtIsNull(recipient.getId())
        );
    }

    @Override
    public void markRead(Long notificationId, String email) {
        User recipient = getUser(email);
        Notification notification = notificationRepository
                .findByIdAndRecipient_Id(notificationId, recipient.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
        }
    }

    @Override
    public void markAllRead(String email) {
        User recipient = getUser(email);
        LocalDateTime readAt = LocalDateTime.now();
        notificationRepository.findByRecipient_IdAndReadAtIsNull(recipient.getId())
                .forEach(notification -> notification.setReadAt(readAt));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getActionUrl(),
                notification.getReadAt() != null,
                notification.getCreatedAt()
        );
    }
}
