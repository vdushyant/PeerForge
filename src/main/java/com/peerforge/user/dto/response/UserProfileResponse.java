package com.peerforge.user.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record UserProfileResponse(

        Long id,

        String headline,

        String bio,

        BigDecimal yearsOfExperience,

        String githubUrl,

        String linkedinUrl,

        List<String> skills
) {
}