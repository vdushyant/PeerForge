package com.peerforge.session.service.impl;

import com.peerforge.common.exception.*;
import com.peerforge.mentor.entity.ApprovalStatus;
import com.peerforge.mentor.entity.MentorAvailability;
import com.peerforge.mentor.entity.MentorProfile;
import com.peerforge.mentor.repository.MentorAvailabilityRepository;
import com.peerforge.mentor.repository.MentorProfileRepository;
import com.peerforge.notification.entity.NotificationType;
import com.peerforge.notification.service.NotificationService;
import com.peerforge.payment.entity.MentorPayout;
import com.peerforge.payment.entity.MentorPayoutStatus;
import com.peerforge.payment.entity.PaymentStatus;
import com.peerforge.payment.repository.MentorPayoutRepository;
import com.peerforge.role.entity.Role;
import com.peerforge.session.dto.request.BookSessionRequest;
import com.peerforge.session.dto.request.SendSessionMessageRequest;
import com.peerforge.session.dto.response.SessionMessageResponse;
import com.peerforge.session.dto.response.SessionResponse;
import com.peerforge.session.entity.Session;
import com.peerforge.session.entity.SessionStatus;
import com.peerforge.session.mapper.SessionMapper;
import com.peerforge.session.repository.SessionRepository;
import com.peerforge.session.repository.SessionMessageRepository;
import com.peerforge.session.entity.SessionMessage;
import com.peerforge.session.service.SessionService;
import com.peerforge.user.entity.User;
import com.peerforge.user.repository.UserRepository;
import lombok.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionServiceImpl implements SessionService {
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final MentorAvailabilityRepository mentorAvailabilityRepository;
    private final SessionMapper sessionMapper;
    private final NotificationService notificationService;
    private final SessionMessageRepository sessionMessageRepository;
    private final MentorPayoutRepository mentorPayoutRepository;

    private static final DateTimeFormatter NOTIFICATION_DATE =
            DateTimeFormatter.ofPattern("EEE, d MMM 'at' HH:mm");

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

        if (mentor.getUser().getId().equals(client.getId())) {
            throw new BookingConflictException(
                    "You cannot book a session with yourself"
            );
        }

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
        notificationService.notifyUser(
                mentor.getUser(),
                NotificationType.BOOKING_REQUESTED,
                "New session request",
                client.getFirstName() + " requested a session for "
                        + request.startDateTime().format(NOTIFICATION_DATE) + ".",
                "/mentor/sessions"
        );
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
    public List<SessionResponse> getMentorSessions(String email) {

        User mentorUser = getCurrentUser(email);

        MentorProfile mentor = mentorProfileRepository
                .findByUserId(mentorUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Mentor profile not found"
                        ));

        return sessionRepository
                .findByMentorIdOrderByStartDateTimeAsc(mentor.getId())
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
        notificationService.notifyUser(
                session.getClient(),
                NotificationType.SESSION_CONFIRMED,
                "Session confirmed",
                "Your session with " + session.getMentor().getUser().getFirstName()
                        + " is confirmed for " + session.getStartDateTime().format(NOTIFICATION_DATE) + ".",
                "/sessions"
        );
        return sessionMapper.toResponse(saved);
    }

    @Override
    @CacheEvict(cacheNames = "landingStats", allEntries = true)
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
        if (mentorPayoutRepository.findByPayment_Id(session.getPayment().getId()).isEmpty()) {
            mentorPayoutRepository.save(MentorPayout.builder()
                    .payment(session.getPayment())
                    .mentor(session.getMentor())
                    .amount(session.getPayment().getMentorAmount())
                    .status(MentorPayoutStatus.PENDING_ROUTE_SETUP)
                    .build());
        }
        notificationService.notifyUser(
                session.getClient(),
                NotificationType.SESSION_COMPLETED,
                "Session completed",
                "Your session with " + session.getMentor().getUser().getFirstName()
                        + " is complete. You can now leave a review.",
                "/sessions"
        );
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
        String sessionTime = session.getStartDateTime().format(NOTIFICATION_DATE);
        boolean cancelledByClient = session.getClient().getId().equals(currentUser.getId());
        boolean cancelledByMentor = session.getMentor().getUser().getId().equals(currentUser.getId());
        if (!cancelledByClient) {
            notificationService.notifyUser(
                    session.getClient(), NotificationType.SESSION_CANCELLED,
                    "Session cancelled",
                    "Your session with " + session.getMentor().getUser().getFirstName()
                            + " scheduled for " + sessionTime + " was cancelled.",
                    "/sessions"
            );
        }
        if (!cancelledByMentor) {
            notificationService.notifyUser(
                    session.getMentor().getUser(), NotificationType.SESSION_CANCELLED,
                    "Session cancelled",
                    "The session with " + session.getClient().getFirstName()
                            + " scheduled for " + sessionTime + " was cancelled.",
                    "/mentor/sessions"
            );
        }
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

    @Override
    @Transactional(readOnly = true)
    public List<SessionMessageResponse> getMessages(Long sessionId, String email) {
        User currentUser = getCurrentUser(email);
        Session session = getSession(sessionId);
        validateConversationAccess(session, currentUser);

        List<SessionMessage> messages = sessionMessageRepository
                .findTop100BySession_IdOrderByCreatedAtDescIdDesc(sessionId);
        Collections.reverse(messages);
        return messages.stream().map(message -> toMessageResponse(message, currentUser)).toList();
    }

    @Override
    public SessionMessageResponse sendMessage(
            Long sessionId,
            SendSessionMessageRequest request,
            String email
    ) {
        User currentUser = getCurrentUser(email);
        Session session = getSession(sessionId);
        validateConversationAccess(session, currentUser);

        String text = request.message().trim();
        if (text.isEmpty()) {
            throw new BookingConflictException("Message cannot be blank");
        }

        SessionMessage saved = sessionMessageRepository.save(SessionMessage.builder()
                .session(session)
                .sender(currentUser)
                .message(text)
                .build());
        User recipient = session.getClient().getId().equals(currentUser.getId())
                ? session.getMentor().getUser()
                : session.getClient();
        notificationService.notifyUser(
                recipient,
                NotificationType.SESSION_MESSAGE,
                "New session message",
                currentUser.getFirstName() + " sent you a message about your session.",
                recipient.getId().equals(session.getMentor().getUser().getId())
                        ? "/mentor/sessions"
                        : "/sessions"
        );
        return toMessageResponse(saved, currentUser);
    }

    @Override
    public SessionResponse updateMeetingLink(Long sessionId, String meetingLink, String email) {
        User currentUser = getCurrentUser(email);
        Session session = getSession(sessionId);
        if (!session.getMentor().getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the session mentor can update its meeting link");
        }
        if (session.getStatus() != SessionStatus.CONFIRMED
                || session.getPayment() == null
                || session.getPayment().getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidSessionStateException("A meeting link can only be set for a paid, confirmed session");
        }
        session.setMeetingLink(meetingLink.trim());
        return sessionMapper.toResponse(sessionRepository.save(session));
    }

    private SessionMessageResponse toMessageResponse(SessionMessage message, User currentUser) {
        User sender = message.getSender();
        return new SessionMessageResponse(
                message.getId(),
                sender.getFirstName() + " " + sender.getLastName(),
                message.getMessage(),
                message.getCreatedAt(),
                sender.getId().equals(currentUser.getId())
        );
    }

    private void validateConversationAccess(Session session, User currentUser) {
        boolean isMentor = session.getMentor().getUser().getId().equals(currentUser.getId());
        boolean isClient = session.getClient().getId().equals(currentUser.getId());
        if (!isMentor && !isClient) {
            throw new AccessDeniedException("Only the session mentor and client can access its conversation");
        }
        boolean isAvailableStatus = session.getStatus() == SessionStatus.CONFIRMED
                || session.getStatus() == SessionStatus.COMPLETED;
        boolean paymentSuccessful = session.getPayment() != null
                && session.getPayment().getStatus() == PaymentStatus.SUCCESS;
        if (!isAvailableStatus || !paymentSuccessful) {
            throw new InvalidSessionStateException("Messaging is available only for paid, confirmed sessions");
        }
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
        if (session.getPayment() == null || session.getPayment().getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidSessionStateException("A session can only be confirmed after successful payment");
        }
    }

    private void validateCompleteTransition(Session session) {
        if (session.getStatus() != SessionStatus.CONFIRMED) {
            throw new InvalidSessionStateException(
                    "Only confirmed sessions can be completed"
            );
        }
        if (session.getPayment() == null || session.getPayment().getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidSessionStateException("Only paid sessions can be completed");
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
