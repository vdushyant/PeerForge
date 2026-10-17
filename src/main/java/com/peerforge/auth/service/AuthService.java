package com.peerforge.auth.service;

import com.peerforge.auth.dto.request.LoginRequest;
import com.peerforge.auth.dto.request.LogoutRequest;
import com.peerforge.auth.dto.request.RefreshTokenRequest;
import com.peerforge.auth.dto.request.RegisterRequest;
import com.peerforge.auth.dto.response.AuthenticationResponse;
import com.peerforge.auth.dto.response.MessageResponse;

public interface AuthService {

    MessageResponse register(RegisterRequest request);

    MessageResponse verifyEmail(String token);

    MessageResponse resendVerification(String email);

    MessageResponse requestPasswordReset(String email);

    MessageResponse resetPassword(String token, String newPassword);

    AuthenticationResponse login(
            LoginRequest request
    );

    AuthenticationResponse refresh(
            RefreshTokenRequest request
    );
    void logout(LogoutRequest request);
}
