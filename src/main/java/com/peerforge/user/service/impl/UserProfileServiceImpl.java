package com.peerforge.user.service.impl;

import com.peerforge.common.exception.DuplicateResourceException;
import com.peerforge.common.exception.ResourceNotFoundException;
import com.peerforge.skill.entity.Skill;
import com.peerforge.skill.repository.SkillRepository;
import com.peerforge.user.dto.request.CreateProfileRequest;
import com.peerforge.user.dto.request.UpdateProfileRequest;
import com.peerforge.user.dto.response.UserProfileResponse;
import com.peerforge.user.entity.User;
import com.peerforge.user.entity.UserProfile;
import com.peerforge.user.mapper.UserProfileMapper;
import com.peerforge.user.repository.UserProfileRepository;
import com.peerforge.user.repository.UserRepository;
import com.peerforge.user.service.UserProfileService;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final SkillRepository skillRepository;

    @Override
    public UserProfileResponse createProfile(CreateProfileRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        if (userProfileRepository.findByUserId(user.getId())
                .isPresent()) {
            throw new DuplicateResourceException(
                    "Profile already exists"
            );
        }

        UserProfile profile = userProfileMapper.toEntity(request);

        profile.setUser(user);

        UserProfile savedProfile = userProfileRepository.save(profile);

        return userProfileMapper.toResponse(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        UserProfile profile =
                userProfileRepository.findByUserId(
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Profile not found"
                                ));

        return userProfileMapper.toResponse(profile);
    }

    @Override
    public UserProfileResponse updateProfile(
            UpdateProfileRequest request,
            String email
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        UserProfile profile =
                userProfileRepository.findByUserId(
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Profile not found"
                                ));

        userProfileMapper.updateProfile(
                request,
                profile
        );

        UserProfile updatedProfile = userProfileRepository.save(profile);

        return userProfileMapper.toResponse(
                updatedProfile
        );
    }

    @Override
    public void addSkillToCurrentUser(
            Long skillId,
            String email
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Skill not found"
                        ));

        user.getSkills().add(skill);

        userRepository.save(user);
    }

    @Override
    public void removeSkillFromCurrentUser(
            Long skillId,
            String email
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Skill not found"
                        ));

        user.getSkills().remove(skill);

        userRepository.save(user);
    }

}