package com.peerforge.auth.repository;

import com.peerforge.auth.entity.RefreshToken;
import com.peerforge.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);

    @Modifying
    @Query("""
            DELETE FROM RefreshToken rt
            WHERE rt.revoked = true
               OR rt.expiryDate < :now
            """)
    int deleteExpiredAndRevoked(
            @Param("now") LocalDateTime now
    );
}