package com.peerforge.auth.service.impl;

import com.peerforge.auth.dto.request.LoginRequest;
import com.peerforge.auth.dto.request.RegisterRequest;
import com.peerforge.auth.dto.response.AuthenticationResponse;
import com.peerforge.auth.entity.RefreshToken;
import com.peerforge.auth.mapper.AuthMapper;
import com.peerforge.auth.repository.RefreshTokenRepository;
import com.peerforge.auth.security.CustomUserDetails;
import com.peerforge.auth.service.AuthService;
import com.peerforge.auth.service.JwtService;
import com.peerforge.common.exception.DuplicateResourceException;
import com.peerforge.common.exception.ResourceNotFoundException;
import com.peerforge.role.entity.Role;
import com.peerforge.role.repository.RoleRepository;
import com.peerforge.user.entity.AccountStatus;
import com.peerforge.user.entity.User;
import com.peerforge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = authMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.password()));

        user.setAccountStatus(AccountStatus.ACTIVE);

        Role clientRole = roleRepository
                .findByName("CLIENT")
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "CLIENT role not found"
                        )
                );

        user.getRoles().add(clientRole);

        User savedUser = userRepository.save(user);

        UserDetails userDetails = new CustomUserDetails(savedUser);

        String accessToken = jwtService.generateToken(userDetails);

        String refreshTokenValue = jwtService.generateRefreshToken(userDetails);

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
                        .user(savedUser)
                        .build();

        refreshTokenRepository.save(refreshToken);

        return new AuthenticationResponse(
                accessToken,
                refreshTokenValue
        );
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

        UserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtService.generateToken(userDetails);

        String refreshTokenValue = jwtService.generateRefreshToken(userDetails);

        RefreshToken refreshToken = RefreshToken.builder()
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
}