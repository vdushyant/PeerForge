package com.peerforge.user.controller;

import com.peerforge.user.dto.request.CreateProfileRequest;
import com.peerforge.user.dto.request.UpdateProfileRequest;
import com.peerforge.user.dto.response.UserProfileResponse;
import com.peerforge.user.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserProfileResponse createProfile(
            @Valid @RequestBody CreateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return userProfileService.createProfile(
                request,
                userDetails.getUsername()
        );
    }

    @GetMapping("/me")
    public UserProfileResponse getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return userProfileService.getMyProfile(
                userDetails.getUsername()
        );
    }

    @PutMapping("/me")
    public UserProfileResponse updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        return userProfileService.updateProfile(
                request,
                userDetails.getUsername()
        );
    }
}