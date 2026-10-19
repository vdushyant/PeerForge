package com.peerforge.session.dto.response;

import java.time.LocalDateTime;

public record SessionMessageResponse(
        Long id,
        String senderName,
        String message,
        LocalDateTime createdAt,
        boolean sentByMe
) {
}
