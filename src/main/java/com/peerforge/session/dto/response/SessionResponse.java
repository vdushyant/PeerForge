package com.peerforge.session.dto.response;

import com.peerforge.session.entity.SessionStatus;

import java.time.LocalDateTime;

public record SessionResponse(

        Long id,

        Long mentorId,

        String mentorName,

        String mentorEmail,

        Long clientId,

        String clientName,

        String clientEmail,

        LocalDateTime startDateTime,

        LocalDateTime endDateTime,

        SessionStatus status

) {
}