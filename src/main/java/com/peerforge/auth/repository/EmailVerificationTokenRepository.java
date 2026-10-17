package com.peerforge.auth.repository;

import com.peerforge.auth.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenHashAndUsedFalse(String tokenHash);

    void deleteAllByUser_Id(Long userId);
}
