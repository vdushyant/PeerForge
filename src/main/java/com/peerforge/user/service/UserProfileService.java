package com.peerforge.user.service;

import com.peerforge.user.dto.request.CreateProfileRequest;
import com.peerforge.user.dto.request.UpdateProfileRequest;
import com.peerforge.user.dto.response.UserProfileResponse;

public interface UserProfileService {

    UserProfileResponse createProfile(CreateProfileRequest request, String email);
    UserProfileResponse getMyProfile(String email);
    UserProfileResponse updateProfile(UpdateProfileRequest request, String email);
}