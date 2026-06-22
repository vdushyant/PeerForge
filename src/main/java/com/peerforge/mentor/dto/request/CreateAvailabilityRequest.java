package com.peerforge.mentor.dto.request;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record CreateAvailabilityRequest(
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
) {
}