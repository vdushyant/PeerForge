package com.peerforge.auth.dto.response;

public record AuthenticationResponse(
        String accessToken,
        String refreshToken
) {
}