package com.peerforge.session.controller;

import com.peerforge.session.dto.request.BookSessionRequest;
import com.peerforge.session.dto.response.SessionResponse;
import com.peerforge.session.service.SessionService;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController

@RequestMapping("/api/v1/sessions")

@RequiredArgsConstructor

public class SessionController {
    private final SessionService sessionService;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse bookSession(
            @Valid
            @RequestBody
            BookSessionRequest request,
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        return sessionService.bookSession(request, userDetails.getUsername());
    }

}