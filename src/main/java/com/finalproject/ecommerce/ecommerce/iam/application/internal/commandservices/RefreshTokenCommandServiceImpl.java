package com.finalproject.ecommerce.ecommerce.iam.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.iam.application.internal.outboundservices.tokens.TokenService;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.RefreshToken;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.RefreshTokenCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.RevokeAllUserRefreshTokensCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.RevokeRefreshTokenCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignOutCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.RefreshTokenCommandService;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.RefreshTokenRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration.properties.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;


//Later implementation HttpOnlY Cookie
@Slf4j
@Service
public class RefreshTokenCommandServiceImpl implements RefreshTokenCommandService {

    private static final int TOKEN_LENGTH = 64;
    private final SecureRandom secureRandom = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;

    @Resource
    private RefreshTokenCommandService refreshTokenCommandService;

    public RefreshTokenCommandServiceImpl(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, TokenService tokenService, JwtProperties jwtProperties) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    @Transactional
    public String createRefreshToken(User user) {
        log.info("Creating refresh token for user: {}", user.getUsername());

        // Currently this has been added so that only one refresh token is valid at a time
        // In the future, we might want to allow multiple tokens per user (e.g. for multiple devices)
        revokeAllUserTokens(user);

        String plainToken = generateSecureToken();
        String tokenHash = hashToken(plainToken);

        Instant expiresAt = Instant.now().plus(jwtProperties.getRefreshToken().getExpirationDays(), ChronoUnit.DAYS);

        RefreshToken refreshToken = new RefreshToken(tokenHash, user, expiresAt);

        refreshTokenRepository.save(refreshToken);

        log.info("Refresh token created successfully for user: {}", user.getUsername());

        return plainToken;
    }

    @Override
    @Transactional
    public Optional<ImmutablePair<ImmutablePair<User, String>, String>> handle(RefreshTokenCommand command) {
        log.info("Processing refresh token request");

        String tokenHash = hashToken(command.refreshToken());

        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findWithLockByTokenHash(tokenHash);

        if (refreshTokenOpt.isEmpty()) {
            log.warn("Refresh token not found - possible attack or invalid token");
            return Optional.empty();
        }

        RefreshToken refreshToken = refreshTokenOpt.get();

        if (refreshToken.isUsed()) {
            revokeAllUserTokens(refreshToken.getUser());
            throw new SecurityException("Token reuse detected - all tokens revoked");
        }

        if (!refreshToken.isValid()) {
            log.warn("Invalid refresh token - revoked: {}, expired: {}", refreshToken.isRevoked(), refreshToken.isExpired());
            return Optional.empty();
        }

        User user = refreshToken.getUser();
        if (user == null) {
            log.error("User not found for refresh token");
            return Optional.empty();
        }

        refreshToken.markAsUsed();
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = tokenService.generateToken(user.getUsername());

        String newRefreshToken = refreshTokenCommandService.createRefreshToken(user);

        log.info("Token refresh successful for user: {}", user.getUsername());

        return Optional.of(ImmutablePair.of(ImmutablePair.of(user, newAccessToken), newRefreshToken));
    }

    @Override
    @Transactional
    public void handle(RevokeRefreshTokenCommand command) {
        log.info("Revoking refresh token");

        String tokenHash = hashToken(command.refreshToken());
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);

        if (refreshTokenOpt.isEmpty()) {
            log.warn("Refresh token not found for revocation");
            return;
        }

        RefreshToken refreshToken = refreshTokenOpt.get();
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        log.info("Refresh token revoked successfully");
    }

    @Override
    @Transactional
    public void handle(RevokeAllUserRefreshTokensCommand command) {
        log.info("Revoking all refresh tokens for user ID: {}", command.userId());

        Optional<User> userOpt = userRepository.findById(command.userId());
        if (userOpt.isEmpty()) {
            log.warn("User not found for revoking tokens: {}", command.userId());
            return;
        }

        revokeAllUserTokens(userOpt.get());
    }

    @Override
    @Transactional
    public void handle(SignOutCommand command) {
        log.info("Sign-out requested for user: {}", command.username());

        Optional<User> userOpt = userRepository.findByUsername(command.username());
        if (userOpt.isEmpty()) {
            log.warn("User not found for sign-out: {}", command.username());
            return;
        }

        User user = userOpt.get();
        revokeAllUserTokens(user);

        log.info("Sign-out completed for user: {}", command.username());
    }

    private void revokeAllUserTokens(User user) {
        var tokens = refreshTokenRepository.findByUserAndRevoked(user, false);
        tokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(tokens);
        log.info("Revoked {} tokens for user: {}", tokens.size(), user.getUsername());
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashToken(String plainToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainToken.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
