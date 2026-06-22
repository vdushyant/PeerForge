package com.peerforge.admin.controller;

import com.peerforge.mentor.dto.response.MentorProfileResponse;
import com.peerforge.mentor.service.MentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MentorService mentorService;

    @PatchMapping(
            "/mentors/{mentorId}/approve"
    )

    public MentorProfileResponse approveMentor(

            @PathVariable
            Long mentorId
    ) {

        return mentorService
                .approveMentor(
                        mentorId
                );
    }

}