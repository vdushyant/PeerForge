package com.peerforge.user.mapper;

import com.peerforge.user.dto.request.CreateProfileRequest;
import com.peerforge.user.dto.request.UpdateProfileRequest;
import com.peerforge.user.dto.response.UserProfileResponse;
import com.peerforge.user.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    UserProfile toEntity(
            CreateProfileRequest request
    );

    UserProfileResponse toResponse(
            UserProfile profile
    );

    void updateProfile(
            UpdateProfileRequest request,
            @MappingTarget UserProfile profile
    );
}