package com.peerforge.mentor.mapper;

import com.peerforge.mentor.dto.request.CreateAvailabilityRequest;
import com.peerforge.mentor.dto.request.MentorApplicationRequest;
import com.peerforge.mentor.dto.response.AvailabilityResponse;
import com.peerforge.mentor.dto.response.MentorProfileResponse;
import com.peerforge.mentor.entity.MentorAvailability;
import com.peerforge.mentor.entity.MentorProfile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MentorProfileMapper {

    MentorProfile toEntity(
            MentorApplicationRequest request
    );

    MentorProfileResponse toResponse(
            MentorProfile mentorProfile
    );

    MentorAvailability toEntity(
            CreateAvailabilityRequest request
    );

    AvailabilityResponse
    toResponse(
            MentorAvailability availability
    );
}