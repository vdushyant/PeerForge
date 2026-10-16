package com.peerforge.notification.dto;

import java.util.List;

public record NotificationInboxResponse(
        List<NotificationResponse> notifications,
        long unreadCount
) { }
