package com.peerforge.session.service;

import com.peerforge.session.dto.request.BookSessionRequest;
import com.peerforge.session.dto.response.SessionResponse;

public interface SessionService {

    SessionResponse bookSession(
            BookSessionRequest request,
            String email
    );

}