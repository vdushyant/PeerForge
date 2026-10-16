package com.peerforge.mentor.service;

import com.peerforge.mentor.dto.request.CreateAvailabilityRequest;
import com.peerforge.mentor.dto.request.MentorApplicationRequest;
import com.peerforge.mentor.dto.request.UpdateMentorProfileRequest;
import com.peerforge.mentor.dto.response.AvailabilityResponse;
import com.peerforge.mentor.dto.response.MentorCardResponse;
import com.peerforge.mentor.dto.response.MentorDetailResponse;
import com.peerforge.mentor.dto.response.MentorProfileResponse;
import com.peerforge.mentor.dto.response.MentorApplicationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    MentorProfileResponse rejectMentor(Long mentorId, String reason);

    Page<MentorApplicationResponse> getMentorApplications(
            com.peerforge.mentor.entity.ApprovalStatus status,
            Pageable pageable
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
