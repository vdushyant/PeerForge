package com.peerforge.user.mapper;

import com.peerforge.skill.entity.Skill;
import com.peerforge.user.dto.request.CreateProfileRequest;
import com.peerforge.user.dto.request.UpdateProfileRequest;
import com.peerforge.user.dto.response.UserProfileResponse;
import com.peerforge.user.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    UserProfile toEntity(
            CreateProfileRequest request
    );

//    UserProfileResponse toResponse(
//            UserProfile profile
//    );

    default UserProfileResponse toResponse(
            UserProfile profile
    ) {

        List<String> skills =
                profile.getUser()
                        .getSkills()
                        .stream()
                        .map(Skill::getName)
                        .sorted()
                        .toList();

        return new UserProfileResponse(
                profile.getId(),
                profile.getHeadline(),
                profile.getBio(),
                profile.getYearsOfExperience(),
                profile.getGithubUrl(),
                profile.getLinkedinUrl(),
                skills
        );
    }

    void updateProfile(
            UpdateProfileRequest request,
            @MappingTarget UserProfile profile
    );
}