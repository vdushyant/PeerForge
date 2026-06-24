package com.peerforge.session.service;

import com.peerforge.session.dto.request.BookSessionRequest;
import com.peerforge.session.dto.response.SessionResponse;

import java.util.List;

public interface SessionService {

    SessionResponse bookSession(
            BookSessionRequest request,
            String email
    );

    List<SessionResponse> getMySessions(String email);
    List<SessionResponse> getMentorSessions(Long mentorId);
    SessionResponse confirmSession(Long sessionId, String email);
    SessionResponse cancelSession(Long sessionId, String email);
    SessionResponse completeSession(Long sessionId, String email);
    SessionResponse getSessionById(Long sessionId, String email);
}