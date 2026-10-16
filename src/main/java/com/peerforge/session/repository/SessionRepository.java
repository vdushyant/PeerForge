package com.peerforge.session.repository;

import com.peerforge.session.entity.Session;
import com.peerforge.session.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface SessionRepository
        extends JpaRepository<Session, Long> {

    List<Session> findByMentorId(Long mentorId);
    List<Session> findByClientId(Long clientId);
    List<Session> findByClientIdOrderByStartDateTimeAsc(Long clientId);
    List<Session> findByMentorIdOrderByStartDateTimeAsc(Long mentorId);
    long countByStatus(SessionStatus status);

    long countByMentorIdAndStatus(Long mentorId, SessionStatus status);

    long countByMentorIdAndStatusAndStartDateTimeAfter(
            Long mentorId,
            SessionStatus status,
            LocalDateTime startDateTime
    );

    long countByCreatedAtAfter(LocalDateTime createdAt);
}
