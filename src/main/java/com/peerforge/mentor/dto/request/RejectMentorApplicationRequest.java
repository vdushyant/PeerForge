package com.peerforge.mentor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectMentorApplicationRequest(
        @NotBlank(message = "A rejection reason is required")
        @Size(max = 500, message = "Rejection reason must be 500 characters or fewer")
        String reason
) { }
