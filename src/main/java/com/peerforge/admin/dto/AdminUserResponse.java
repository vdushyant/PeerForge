package com.peerforge.admin.dto;

import com.peerforge.user.entity.AccountStatus;

import java.time.LocalDateTime;
import java.util.List;

public record AdminUserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        AccountStatus accountStatus,
        List<String> roles,
        LocalDateTime createdAt
) { }
