package com.peerforge.admin.dto;

import com.peerforge.payment.entity.PaymentStatus;
import com.peerforge.session.entity.SessionStatus;

import java.time.LocalDateTime;

public record AdminSessionResponse(
        Long id,
        String mentorName,
        String clientName,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        SessionStatus status,
        PaymentStatus paymentStatus
) { }
