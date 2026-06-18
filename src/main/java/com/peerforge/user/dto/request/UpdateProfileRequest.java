package com.peerforge.user.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProfileRequest(

        @Size(max = 150)
        String headline,

        @Size(max = 1000)
        String bio,

        @DecimalMin("0.0")
        BigDecimal yearsOfExperience,

        String githubUrl,

        String linkedinUrl
) {
}