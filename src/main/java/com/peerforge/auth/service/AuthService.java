package com.peerforge.auth.service;

import com.peerforge.auth.dto.request.LoginRequest;
import com.peerforge.auth.dto.request.RegisterRequest;
import com.peerforge.auth.dto.response.AuthenticationResponse;

public interface AuthService {

    AuthenticationResponse register(
            RegisterRequest request
    );

    AuthenticationResponse login(
            LoginRequest request
    );
}