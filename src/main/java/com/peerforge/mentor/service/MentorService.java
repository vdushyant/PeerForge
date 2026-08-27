package com.peerforge.mentor.service;

import com.peerforge.mentor.dto.request.CreateAvailabilityRequest;
import com.peerforge.mentor.dto.request.MentorApplicationRequest;
import com.peerforge.mentor.dto.request.UpdateMentorProfileRequest;
import com.peerforge.mentor.dto.response.AvailabilityResponse;
import com.peerforge.mentor.dto.response.MentorCardResponse;
import com.peerforge.mentor.dto.response.MentorDetailResponse;
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

    MentorProfileResponse getMyMentorProfile(String email);

    MentorProfileResponse updateMyMentorProfile(
            UpdateMentorProfileRequest request,
            String email
    );

    AvailabilityResponse addAvailability(
            CreateAvailabilityRequest request,
            String email
    );

    List<AvailabilityResponse> getMyAvailability(String email);
    void deleteAvailability(Long availabilityId, String email);
    MentorDetailResponse getMentorById(Long mentorId);
    List<AvailabilityResponse> getMentorAvailability(Long mentorId);

}