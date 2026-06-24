package com.peerforge.session.service.impl;

import com.peerforge.common.exception.ResourceNotFoundException;
import com.peerforge.mentor.entity.ApprovalStatus;
import com.peerforge.mentor.entity.MentorAvailability;
import com.peerforge.mentor.entity.MentorProfile;
import com.peerforge.mentor.repository.MentorAvailabilityRepository;
import com.peerforge.mentor.repository.MentorProfileRepository;
import com.peerforge.session.dto.request.BookSessionRequest;
import com.peerforge.session.dto.response.SessionResponse;
import com.peerforge.session.entity.Session;
import com.peerforge.session.entity.SessionStatus;
import com.peerforge.session.mapper.SessionMapper;
import com.peerforge.session.repository.SessionRepository;
import com.peerforge.session.service.SessionService;
import com.peerforge.user.entity.User;
import com.peerforge.user.repository.UserRepository;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionServiceImpl implements SessionService {
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final MentorAvailabilityRepository mentorAvailabilityRepository;
    private final SessionMapper sessionMapper;

    @Override
    public SessionResponse bookSession(
            BookSessionRequest request,
            String email
    ) {
        User client = userRepository.findByEmail(email).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"));

        MentorProfile mentor = mentorProfileRepository
                .findById(request.mentorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Mentor not found"));

        if (mentor.getApprovalStatus()
                != ApprovalStatus.APPROVED) {
            throw new IllegalArgumentException(
                    "Mentor is not approved"
            );
        }

        if (!request.startDateTime().isBefore(request.endDateTime())) {
            throw new IllegalArgumentException(
                    "Start time must be before end time"
            );
        }

        DayOfWeek day = request.startDateTime().getDayOfWeek();
        List<MentorAvailability> slots =
                mentorAvailabilityRepository
                        .findByMentorProfileId(
                                mentor.getId()
                        );


        boolean available = slots
                .stream()
                .anyMatch(slot ->
                        slot.getDayOfWeek() == day
                                &&
                                !request.startDateTime()
                                        .toLocalTime()
                                        .isBefore(
                                                slot.getStartTime()
                                        )
                                &&
                                !request.endDateTime()
                                        .toLocalTime()
                                        .isAfter(slot.getEndTime())
                );

        if (!available) {
            throw new IllegalArgumentException("Mentor unavailable");
        }

        List<Session> mentorSessions = sessionRepository.findByMentorId(mentor.getId());
        for (Session session : mentorSessions) {
            boolean noOverlap = session.getEndDateTime()
                            .compareTo(request.startDateTime()) <= 0 || request.endDateTime()
                            .compareTo(session.getStartDateTime()) <= 0;

            if (!noOverlap) {throw new IllegalArgumentException(
                        "Mentor already booked");
            }
        }

        List<Session> clientSessions = sessionRepository
                        .findByClientId(
                                client.getId());

        for (Session session : clientSessions) {
            boolean noOverlap = session.getEndDateTime()
                            .compareTo(request.startDateTime()) <= 0 || request.endDateTime()
                            .compareTo(session.getStartDateTime()) <= 0;
            if (!noOverlap) {
                throw new IllegalArgumentException(
                        "Client already booked"
                );
            }
        }

        Session session = Session.builder()
                        .mentor(mentor)
                        .client(client)
                        .startDateTime(request.startDateTime())
                        .endDateTime(request.endDateTime())
                        .status(SessionStatus.PENDING)
                        .build();

        Session saved = sessionRepository.save(session);
        return sessionMapper.toResponse(saved);

    }

}