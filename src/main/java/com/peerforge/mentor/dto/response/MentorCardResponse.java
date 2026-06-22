package com.peerforge.mentor.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record MentorCardResponse(

        Long mentorId,

        String firstName,

        String lastName,

        String headline,

        BigDecimal yearsOfExperience,

        List<String> skills,

        BigDecimal hourlyRate,

        BigDecimal averageRating
) {
}