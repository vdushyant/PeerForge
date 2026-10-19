package com.peerforge.session.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendSessionMessageRequest(
        @NotBlank @Size(max = 2000) String message
) {
}
