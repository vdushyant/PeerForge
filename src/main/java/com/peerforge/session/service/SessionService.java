package com.peerforge.session.service;

import com.peerforge.session.dto.request.BookSessionRequest;
import com.peerforge.session.dto.response.SessionResponse;
import com.peerforge.session.dto.response.SessionMessageResponse;
import com.peerforge.session.dto.request.SendSessionMessageRequest;

import java.util.List;

public interface SessionService {

    SessionResponse bookSession(
            BookSessionRequest request,
            String email
    );

    List<SessionResponse> getMySessions(String email);
    List<SessionResponse> getMentorSessions(String email);
    SessionResponse confirmSession(Long sessionId, String email);
    SessionResponse cancelSession(Long sessionId, String email);
    SessionResponse completeSession(Long sessionId, String email);
    SessionResponse getSessionById(Long sessionId, String email);
    List<SessionMessageResponse> getMessages(Long sessionId, String email);
    SessionMessageResponse sendMessage(Long sessionId, SendSessionMessageRequest request, String email);
    SessionResponse updateMeetingLink(Long sessionId, String meetingLink, String email);
}
