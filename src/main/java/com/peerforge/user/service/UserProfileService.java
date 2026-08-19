package com.peerforge.user.service;

import com.peerforge.user.dto.request.CreateProfileRequest;
import com.peerforge.user.dto.request.UpdateProfileRequest;
import com.peerforge.user.dto.response.UserProfileResponse;
import com.peerforge.user.entity.User;
import com.peerforge.user.entity.UserProfile;

public interface UserProfileService {

    UserProfile createInitialProfile(User user);
    UserProfileResponse getMyProfile(String email);
    UserProfileResponse updateProfile(UpdateProfileRequest request, String email);
    void addSkillToCurrentUser(
            Long skillId,
            String email
    );

    void removeSkillFromCurrentUser(
            Long skillId,
            String email
    );

}