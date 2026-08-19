package com.peerforge.mentor.controller;

import com.peerforge.mentor.dto.request.CreateAvailabilityRequest;
import com.peerforge.mentor.dto.request.MentorApplicationRequest;
import com.peerforge.mentor.dto.request.UpdateMentorProfileRequest;
import com.peerforge.mentor.dto.response.AvailabilityResponse;
import com.peerforge.mentor.dto.response.MentorCardResponse;
import com.peerforge.mentor.dto.response.MentorDetailResponse;
import com.peerforge.mentor.dto.response.MentorProfileResponse;
import com.peerforge.mentor.service.MentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;
    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public MentorProfileResponse apply(
            @Valid
            @RequestBody
            MentorApplicationRequest request,
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        return mentorService
                .applyForMentorship(
                        request,
                        userDetails.getUsername()
                );
    }

    @GetMapping
    public List<MentorCardResponse>
    getAllMentors() {
        return mentorService.getAllMentors();
    }

    @GetMapping("/me")
    public MentorProfileResponse getMyMentorProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return mentorService.getMyMentorProfile(userDetails.getUsername());
    }

    @PutMapping("/me")
    public MentorProfileResponse updateMyMentorProfile(
            @AuthenticationPrincipal UserDetails userDetails,  @Valid @RequestBody UpdateMentorProfileRequest request
    ) {
        return mentorService.updateMyMentorProfile(request,userDetails.getUsername());
    }

    @GetMapping("/{mentorId}")
    public MentorDetailResponse getMentorById(
            @PathVariable Long mentorId
    ) {
        return mentorService.getMentorById(mentorId);
    }


    @PostMapping("/availability")
    @ResponseStatus(HttpStatus.CREATED)
    public AvailabilityResponse
    addAvailability(
            @Valid
            @RequestBody
            CreateAvailabilityRequest request,
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        return mentorService.addAvailability(request,userDetails.getUsername());
    }

    @GetMapping("/availability")
    public List<AvailabilityResponse> getMyAvailability(
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        return mentorService.getMyAvailability(userDetails.getUsername());
    }

    @DeleteMapping("/availability/{availabilityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvailability(
            @PathVariable Long availabilityId,
            @AuthenticationPrincipal
            UserDetails userDetails
    ) {
        mentorService.deleteAvailability(availabilityId, userDetails.getUsername());
    }
}