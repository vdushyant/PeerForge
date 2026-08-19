package com.peerforge.mentor.mapper;

import com.peerforge.mentor.dto.request.CreateAvailabilityRequest;
import com.peerforge.mentor.dto.request.MentorApplicationRequest;
import com.peerforge.mentor.dto.response.AvailabilityResponse;
import com.peerforge.mentor.dto.response.MentorDetailResponse;
import com.peerforge.mentor.dto.response.MentorProfileResponse;
import com.peerforge.mentor.entity.MentorAvailability;
import com.peerforge.mentor.entity.MentorProfile;
import com.peerforge.skill.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MentorProfileMapper {

    MentorProfile toEntity(
            MentorApplicationRequest request
    );

    MentorProfileResponse toResponse(
            MentorProfile mentorProfile
    );

    @Mapping(
            target = "mentorId",
            source = "id"
    )
    @Mapping(
            target = "firstName",
            source = "user.firstName"
    )
    @Mapping(
            target = "lastName",
            source = "user.lastName"
    )
    @Mapping(
            target = "headline",
            source = "user.profile.headline"
    )
    @Mapping(
            target = "yearsOfExperience",
            source = "user.profile.yearsOfExperience"
    )
    @Mapping(
            target = "skills",
            source = "user.skills"
    )
    MentorDetailResponse toDetailResponse(
            MentorProfile mentorProfile
    );

    default String mapSkill(Skill skill) {
        return skill.getName();
    }

    MentorAvailability toEntity(
            CreateAvailabilityRequest request
    );

    AvailabilityResponse toResponse(
            MentorAvailability availability
    );
}