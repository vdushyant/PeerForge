package com.peerforge.session.dto.request;

import java.time.LocalDateTime;

public record BookSessionRequest(

        Long mentorId,

        LocalDateTime startDateTime,

        LocalDateTime endDateTime

) {
}