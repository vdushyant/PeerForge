package com.peerforge.auth.controller;

import com.peerforge.auth.dto.request.LoginRequest;
import com.peerforge.auth.dto.request.RegisterRequest;
import com.peerforge.auth.dto.response.AuthenticationResponse;
import com.peerforge.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthenticationResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthenticationResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }
}