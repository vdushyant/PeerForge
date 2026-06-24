package com.peerforge.session.repository;

import com.peerforge.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository
        extends JpaRepository<Session, Long> {

    List<Session> findByMentorId(Long mentorId);
    List<Session> findByClientId(Long clientId);
    List<Session> findByClientIdOrderByStartDateTimeAsc(Long clientId);
    List<Session> findByMentorIdOrderByStartDateTimeAsc(Long mentorId);
}