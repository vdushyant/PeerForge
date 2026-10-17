package com.peerforge.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AccountTokenRequest(
        @NotBlank String token
) {
}
