package com.peerforge.session.repository;

import com.peerforge.session.entity.SessionMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionMessageRepository extends JpaRepository<SessionMessage, Long> {
    List<SessionMessage> findTop100BySession_IdOrderByCreatedAtDescIdDesc(Long sessionId);
}
