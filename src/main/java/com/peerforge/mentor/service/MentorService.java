package com.peerforge.mentor.service;

import com.peerforge.mentor.dto.request.MentorApplicationRequest;
import com.peerforge.mentor.dto.response.MentorCardResponse;
import com.peerforge.mentor.dto.response.MentorProfileResponse;

import java.util.List;

public interface MentorService {

    MentorProfileResponse applyForMentorship(
            MentorApplicationRequest request,
            String email
    );

    List<MentorCardResponse> getAllMentors();

    MentorProfileResponse approveMentor(
            Long mentorId
    );

}