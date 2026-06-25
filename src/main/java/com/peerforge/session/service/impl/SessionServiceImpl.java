package com.peerforge.session.service.impl;

import com.peerforge.common.exception.*;
import com.peerforge.mentor.entity.ApprovalStatus;
import com.peerforge.mentor.entity.MentorAvailability;
import com.peerforge.mentor.entity.MentorProfile;
import com.peerforge.mentor.repository.MentorAvailabilityRepository;
import com.peerforge.mentor.repository.MentorProfileRepository;
import com.peerforge.role.entity.Role;
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
import org.springframework.security.access.AccessDeniedException;
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
        User client  = getCurrentUser(email);

        MentorProfile mentor = mentorProfileRepository
                .findById(request.mentorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Mentor not found"));

        if (mentor.getApprovalStatus()
                != ApprovalStatus.APPROVED) {
            throw new MentorUnavailableException(
                    "Mentor is not approved"
            );
        }

        if (!request.startDateTime().isBefore(request.endDateTime())) {
            throw new BookingConflictException(
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
            throw new MentorUnavailableException("Mentor unavailable");
        }

        List<Session> mentorSessions = sessionRepository.findByMentorId(mentor.getId());
        for (Session session : mentorSessions) {
            boolean noOverlap = session.getEndDateTime()
                            .compareTo(request.startDateTime()) <= 0 || request.endDateTime()
                            .compareTo(session.getStartDateTime()) <= 0;

            if (!noOverlap) {
                throw new MentorUnavailableException("Mentor already booked");
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
                throw new BookingConflictException("Client already booked");
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

    @Override

    @Transactional(readOnly = true)
    public List<SessionResponse> getMySessions(String email) {
        User client = userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found")
                        );
        return sessionRepository.findByClientIdOrderByStartDateTimeAsc(client.getId())
                        .stream()
                        .map(sessionMapper::toResponse)
                        .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> getMentorSessions(Long mentorId) {
        mentorProfileRepository.findById(mentorId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Mentor not found")
                );

        return sessionRepository.findByMentorIdOrderByStartDateTimeAsc(mentorId)
                        .stream()
                        .map(sessionMapper::toResponse)
                        .toList();
    }

    @Override
    public SessionResponse confirmSession(Long sessionId, String email)
    {

        User currentUser = getCurrentUser(email);
        Session session = getSession(sessionId);
        validateMentorOrAdminAccess(session,currentUser);
        validateConfirmTransition(session);

        session.setStatus(SessionStatus.CONFIRMED);

        Session saved = sessionRepository.save(session);
        return sessionMapper.toResponse(saved);
    }

    @Override
    public SessionResponse completeSession(
            Long sessionId,
            String email
    ) {
        User currentUser = getCurrentUser(email);
        Session session = getSession(sessionId);
        validateMentorOrAdminAccess(session,currentUser);
        validateCompleteTransition(session);

        session.setStatus(SessionStatus.COMPLETED);
        Session saved = sessionRepository.save(session);
        return sessionMapper.toResponse(saved);
    }

    @Override
    public SessionResponse cancelSession(
            Long sessionId,
            String email
    ) {
        User currentUser = getCurrentUser(email);
        Session session = getSession(sessionId);
        validateSessionAccess(session,currentUser);
        validateCancelTransition(session);

        session.setStatus(SessionStatus.CANCELLED);
        Session saved = sessionRepository.save(session);
        return sessionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionResponse getSessionById(Long sessionId, String email) {
        User currentUser = getCurrentUser(email);
        Session session = getSession(sessionId);
        validateSessionAccess(session,currentUser);

        return sessionMapper.toResponse(session);
    }

    // Lookup Helpers
    private boolean isAdmin(User user) {
        return user.getRoles()
                .stream()
                .map(Role::getName)
                .anyMatch(
                        "ADMIN"::equals
                );
    }
    private User getCurrentUser(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));
    }

    // Authorization Helpers
    private Session getSession(Long sessionId) {
        return sessionRepository
                .findById(sessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Session not found"
                        ));
    }

    private void validateMentorOrAdminAccess(Session session, User currentUser) {
        boolean isMentor = session.getMentor()
                        .getUser()
                        .getId()
                        .equals(currentUser.getId());
        if (!isAdmin(currentUser) && !isMentor) {
            throw new AccessDeniedException(
                    "Access denied"
            );
        }
    }

    private void validateSessionAccess(Session session, User currentUser) {
        boolean isMentor = session.getMentor()
                        .getUser()
                        .getId()
                        .equals(currentUser.getId());

        boolean isClient = session.getClient()
                        .getId()
                        .equals(currentUser.getId());

        if (!isAdmin(currentUser) && !isMentor && !isClient) {
            throw new AccessDeniedException("Access denied");
        }
    }

    // State Validation Helpers
    private void validateConfirmTransition(Session session) {
        if (session.getStatus() != SessionStatus.PENDING) {
            throw new InvalidSessionStateException(
                    "Only pending sessions can be confirmed"
            );
        }
    }

    private void validateCompleteTransition(Session session) {
        if (session.getStatus() != SessionStatus.CONFIRMED) {
            throw new InvalidSessionStateException(
                    "Only confirmed sessions can be completed"
            );
        }
    }

    private void validateCancelTransition(Session session) {
        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new InvalidSessionStateException("Completed session cannot be cancelled");
        }

        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new InvalidSessionStateException("Session already cancelled");
        }
    }

}