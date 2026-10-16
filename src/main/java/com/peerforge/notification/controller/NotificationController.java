package com.peerforge.notification.controller;

import com.peerforge.notification.dto.NotificationInboxResponse;
import com.peerforge.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public NotificationInboxResponse getInbox(@AuthenticationPrincipal UserDetails userDetails) {
        return notificationService.getInbox(userDetails.getUsername());
    }

    @PatchMapping("/{id}/read")
    public void markRead(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markRead(id, userDetails.getUsername());
    }

    @PatchMapping("/read-all")
    public void markAllRead(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllRead(userDetails.getUsername());
    }
}
