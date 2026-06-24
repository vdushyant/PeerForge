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

import java.util.List;

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

    @GetMapping("/me")
    public List<SessionResponse>
    getMySessions(
            @AuthenticationPrincipal
            UserDetails userDetails) {
        return sessionService.getMySessions(userDetails.getUsername());
    }

    @GetMapping("/mentor/{mentorId}")
    public List<SessionResponse>
    getMentorSessions(
            @PathVariable
            Long mentorId) {
        return sessionService.getMentorSessions(mentorId);
    }

    @PatchMapping("/{id}/confirm")
    public SessionResponse confirmSession(
            @PathVariable Long id,
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        return sessionService.confirmSession(id, userDetails.getUsername());
    }

    @PatchMapping("/{id}/cancel")
    public SessionResponse cancelSession(
            @PathVariable Long id,
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        return sessionService.cancelSession(id, userDetails.getUsername());
    }

    @PatchMapping("/{id}/complete")
    public SessionResponse completeSession(
            @PathVariable Long id,
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        return sessionService.completeSession(id, userDetails.getUsername());
    }

    @GetMapping("/{id}")
    public SessionResponse getSessionById(
            @PathVariable Long id,
            @AuthenticationPrincipal
            UserDetails userDetails) {
        return sessionService.getSessionById(id, userDetails.getUsername());
    }

}