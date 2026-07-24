package com.peerforge.auth.service;

public interface RefreshTokenCleanupService {

    void cleanupExpiredRefreshTokens();

}