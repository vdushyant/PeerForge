package com.peerforge.session.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateMeetingLinkRequest(
        @NotBlank
        @Size(max = 500)
        @Pattern(regexp = "^https://\\S+$", message = "Meeting link must use HTTPS")
        String meetingLink
) {
}
