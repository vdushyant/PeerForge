package com.peerforge.mentor.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MentorApplicationRequest(

        @NotBlank
        @Size(max = 2000)
        String about,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal hourlyRate
) {
}