package com.peerforge.user.dto.response;

import java.math.BigDecimal;

public record UserProfileResponse(

        Long id,

        String headline,

        String bio,

        BigDecimal yearsOfExperience,

        String githubUrl,

        String linkedinUrl
) {

}