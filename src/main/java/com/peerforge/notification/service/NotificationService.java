package com.peerforge.notification.service;

import com.peerforge.notification.dto.NotificationInboxResponse;
import com.peerforge.notification.entity.NotificationType;
import com.peerforge.user.entity.User;

public interface NotificationService {

    void notifyUser(User recipient, NotificationType type, String title, String message, String actionUrl);

    NotificationInboxResponse getInbox(String email);

    void markRead(Long notificationId, String email);

    void markAllRead(String email);
}
