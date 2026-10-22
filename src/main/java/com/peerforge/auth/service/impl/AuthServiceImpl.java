package com.peerforge.auth.service.impl;

import com.peerforge.auth.dto.request.LoginRequest;
import com.peerforge.auth.dto.request.LogoutRequest;
import com.peerforge.auth.dto.request.RefreshTokenRequest;
import com.peerforge.auth.dto.request.RegisterRequest;
import com.peerforge.auth.dto.response.AuthenticationResponse;
import com.peerforge.auth.dto.response.MessageResponse;
import com.peerforge.auth.entity.EmailVerificationToken;
import com.peerforge.auth.entity.PasswordResetToken;
import com.peerforge.auth.entity.RefreshToken;
import com.peerforge.auth.mapper.AuthMapper;
import com.peerforge.auth.repository.EmailVerificationTokenRepository;
import com.peerforge.auth.repository.PasswordResetTokenRepository;
import com.peerforge.auth.repository.RefreshTokenRepository;
import com.peerforge.auth.security.CustomUserDetails;
import com.peerforge.auth.service.AccountEmailService;
import com.peerforge.auth.service.AuthService;
import com.peerforge.auth.service.JwtService;
import com.peerforge.common.exception.DuplicateResourceException;
import com.peerforge.common.exception.InvalidRefreshTokenException;
import com.peerforge.common.exception.InvalidAccountTokenException;
import com.peerforge.common.exception.MailDeliveryUnavailableException;
import com.peerforge.common.exception.ResourceNotFoundException;
import com.peerforge.role.entity.Role;
import com.peerforge.role.repository.RoleRepository;
import com.peerforge.user.entity.AccountStatus;
import com.peerforge.user.entity.User;
import com.peerforge.user.service.UserProfileService;
import com.peerforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserProfileService userProfileService;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final AccountEmailService accountEmailService;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;
    private final AuthMapper authMapper;

    @Value("${peerforge.auth.email-enabled:false}")
    private boolean emailEnabled;

    @Override
    @Transactional
    public MessageResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = authMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.password()));

        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(!emailEnabled);

        Role clientRole = roleRepository
                .findByName("CLIENT")
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "CLIENT role not found"
                        )
                );

        user.getRoles().add(clientRole);

        User savedUser = userRepository.save(user);

        userProfileService.createInitialProfile(savedUser);
        if (emailEnabled) {
            sendVerification(savedUser);
            return new MessageResponse("Check your email for a link to verify your PeerForge account.");
        }
        return new MessageResponse("Account created. You can sign in now.");
    }

    @Override
    @Transactional
    public MessageResponse verifyEmail(String rawToken) {
        EmailVerificationToken token = emailVerificationTokenRepository
                .findByTokenHashAndUsedFalse(hashToken(rawToken))
                .orElseThrow(() -> new InvalidAccountTokenException("This verification link is invalid or has expired"));
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidAccountTokenException("This verification link is invalid or has expired");
        }

        token.setUsed(true);
        token.getUser().setEmailVerified(true);
        userRepository.save(token.getUser());
        emailVerificationTokenRepository.save(token);
        return new MessageResponse("Email verified. You can now sign in.");
    }

    @Override
    @Transactional
    public MessageResponse resendVerification(String email) {
        if (!emailEnabled) {
            return new MessageResponse("Email verification is temporarily unavailable.");
        }
        userRepository.findByEmail(email).filter(user -> !Boolean.TRUE.equals(user.getEmailVerified()))
                .ifPresent(user -> {
                    try {
                        sendVerification(user);
                    } catch (MailDeliveryUnavailableException exception) {
                        log.warn("Verification email could not be delivered; SMTP is unavailable");
                    }
                });
        return new MessageResponse("If the account needs verification, a new link will be sent to that address.");
    }

    @Override
    @Transactional
    public MessageResponse requestPasswordReset(String email) {
        if (!emailEnabled) {
            return new MessageResponse("Password reset is temporarily unavailable. Please contact support for help.");
        }
        userRepository.findByEmail(email)
                .filter(user -> Boolean.TRUE.equals(user.getEmailVerified()))
                .ifPresent(user -> {
                    try {
                        sendPasswordReset(user);
                    } catch (MailDeliveryUnavailableException exception) {
                        log.warn("Password reset email could not be delivered; SMTP is unavailable");
                    }
                });
        return new MessageResponse("If a verified account exists for that address, a password reset link will be sent.");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenAndUsedFalse(hashToken(rawToken))
                .orElseThrow(() -> new InvalidAccountTokenException("This password reset link is invalid or has expired"));
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidAccountTokenException("This password reset link is invalid or has expired");
        }

        var user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        token.setUsed(true);
        passwordResetTokenRepository.save(token);
        refreshTokenRepository.deleteByUser(user);
        passwordResetTokenRepository.deleteAllByUser_Id(user.getId());
        return new MessageResponse("Password updated. Sign in with your new password.");
    }

    private void sendVerification(User user) {
        emailVerificationTokenRepository.deleteAllByUser_Id(user.getId());
        String rawToken = generateToken();
        emailVerificationTokenRepository.save(EmailVerificationToken.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build());
        accountEmailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), rawToken);
    }

    private void sendPasswordReset(com.peerforge.user.entity.User user) {
        passwordResetTokenRepository.deleteAllByUser_Id(user.getId());
        String rawToken = generateToken();
        passwordResetTokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .token(hashToken(rawToken))
                .expiryDate(LocalDateTime.now().plusMinutes(20))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build());
        accountEmailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), rawToken);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    @Override
    @Transactional
    public AuthenticationResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository
                .findByEmail(request.email())
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "User not found"
                        )
                );

        return createAuthenticationResponse(user);
    }

    @Override
    @Transactional
    public AuthenticationResponse refresh(
            RefreshTokenRequest request
    ) {
        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(request.refreshToken())
                        .orElseThrow(() ->
                                new InvalidRefreshTokenException("Invalid refresh token"));

        if (refreshToken.getRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return createAuthenticationResponse(refreshToken.getUser());
    }

    private AuthenticationResponse createAuthenticationResponse(
            User user
    ) {
        UserDetails userDetails = new CustomUserDetails(user, emailEnabled);

        String accessToken =
                jwtService.generateToken(userDetails);

        String refreshTokenValue =
                jwtService.generateRefreshToken(userDetails);

        RefreshToken refreshToken =
                RefreshToken.builder()
                        .token(refreshTokenValue)
                        .expiryDate(
                                LocalDateTime.now()
                                        .plusSeconds(
                                                jwtService.getRefreshExpiration() / 1000
                                        )
                        )
                        .revoked(false)
                        .createdAt(LocalDateTime.now())
                        .user(user)
                        .build();

        refreshTokenRepository.save(refreshToken);

        return new AuthenticationResponse(
                accessToken,
                refreshTokenValue
        );
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(request.refreshToken())
                        .orElseThrow(() ->
                                new InvalidRefreshTokenException(
                                        "Invalid refresh token"
                                ));

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }
}
