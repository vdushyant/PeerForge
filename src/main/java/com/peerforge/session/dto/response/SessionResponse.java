package com.peerforge.session.dto.response;

import com.peerforge.session.entity.SessionStatus;

import java.time.LocalDateTime;

public record SessionResponse(

        Long id,

        Long mentorId,

        Long clientId,

        LocalDateTime startDateTime,

        LocalDateTime endDateTime,

        SessionStatus status

) {
}