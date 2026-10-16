package com.peerforge.admin.service;

import com.peerforge.admin.dto.AdminSessionResponse;
import com.peerforge.admin.dto.AdminUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminDirectoryService {

    Page<AdminUserResponse> getUsers(Pageable pageable);

    Page<AdminSessionResponse> getSessions(Pageable pageable);
}
