package com.peerforge.auth.service.impl;

import com.peerforge.auth.repository.RefreshTokenRepository;
import com.peerforge.auth.service.RefreshTokenCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenCleanupServiceImpl
        implements RefreshTokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredRefreshTokens() {

        int deleted = refreshTokenRepository.deleteExpiredAndRevoked(LocalDateTime.now());

        log.info(
                "Deleted {} expired/revoked refresh tokens.",
                deleted
        );
    }
}