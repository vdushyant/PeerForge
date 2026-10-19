package com.peerforge.session.dto.response;

import com.peerforge.payment.entity.PaymentStatus;
import com.peerforge.session.entity.SessionStatus;

import java.time.LocalDateTime;

public record SessionResponse(

        Long id,

        Long mentorId,

        String mentorName,

        Long clientId,

        String clientName,

        LocalDateTime startDateTime,

        LocalDateTime endDateTime,

        SessionStatus status,

        PaymentStatus paymentStatus,

        String meetingLink

) {
}
