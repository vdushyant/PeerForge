package com.peerforge.user.dto.response;

import com.peerforge.role.entity.Role;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record UserProfileResponse(

        Long id,

        String firstName,
        String lastName,
        String email,

        List<String> roles,

        String headline,
        String bio,

        BigDecimal yearsOfExperience,

        String githubUrl,
        String linkedInUrl,

        List<String> skills
) {
}