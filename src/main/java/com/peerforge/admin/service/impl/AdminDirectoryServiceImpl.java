package com.peerforge.admin.service.impl;

import com.peerforge.admin.dto.AdminSessionResponse;
import com.peerforge.admin.dto.AdminUserResponse;
import com.peerforge.admin.service.AdminDirectoryService;
import com.peerforge.session.entity.Session;
import com.peerforge.session.repository.SessionRepository;
import com.peerforge.user.entity.User;
import com.peerforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDirectoryServiceImpl implements AdminDirectoryService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;

    @Override
    public Page<AdminUserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toUserResponse);
    }

    @Override
    public Page<AdminSessionResponse> getSessions(Pageable pageable) {
        return sessionRepository.findAll(pageable).map(this::toSessionResponse);
    }

    private AdminUserResponse toUserResponse(User user) {
        return new AdminUserResponse(
                user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getAccountStatus(),
                user.getRoles().stream().map(role -> role.getName()).sorted().toList(),
                user.getCreatedAt()
        );
    }

    private AdminSessionResponse toSessionResponse(Session session) {
        String mentorName = session.getMentor().getUser().getFirstName() + " "
                + session.getMentor().getUser().getLastName();
        String clientName = session.getClient().getFirstName() + " "
                + session.getClient().getLastName();
        return new AdminSessionResponse(
                session.getId(), mentorName, clientName,
                session.getStartDateTime(), session.getEndDateTime(), session.getStatus(),
                session.getPayment() == null ? null : session.getPayment().getStatus()
        );
    }
}
