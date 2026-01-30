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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;


//Later implementation HttpOnlY Cookie
@Service
public class RefreshTokenCommandServiceImpl implements RefreshTokenCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshTokenCommandServiceImpl.class);
    private static final int TOKEN_LENGTH = 64;
    private final SecureRandom secureRandom = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Value("${authorization.jwt.refresh-token.expiration.days}")
    private int refreshTokenExpirationDays;

    public RefreshTokenCommandServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            TokenService tokenService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public String createRefreshToken(User user) {
        LOGGER.info("Creating refresh token for user: {}", user.getUsername());

        // Currently this has been added so that only one refresh token is valid at a time
        // In the future, we might want to allow multiple tokens per user (e.g. for multiple devices)
        revokeAllUserTokens(user.getId());

        String plainToken = generateSecureToken();
        String tokenHash = hashToken(plainToken);

        Instant expiresAt = Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        RefreshToken refreshToken = new RefreshToken(
            tokenHash,
            user.getId(),
            expiresAt
        );

        refreshTokenRepository.save(refreshToken);

        LOGGER.info("Refresh token created successfully for user: {}", user.getUsername());

        return plainToken;
    }

    @Override
    @Transactional
    public Optional<ImmutablePair<ImmutablePair<User, String>, String>> handle(RefreshTokenCommand command) {
        LOGGER.info("Processing refresh token request");

        String tokenHash = hashToken(command.refreshToken());

        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findWithLockByTokenHash(tokenHash);

        if (refreshTokenOpt.isEmpty()) {
            LOGGER.warn("Refresh token not found - possible attack or invalid token");
            return Optional.empty();
        }

        RefreshToken refreshToken = refreshTokenOpt.get();

        if (refreshToken.isUsed()) {
            revokeAllUserTokens(refreshToken.getUserId());
            throw new SecurityException("Token reuse detected - all tokens revoked");
        }

        if (!refreshToken.isValid()) {
            LOGGER.warn("Invalid refresh token - revoked: {}, expired: {}",
                refreshToken.isRevoked(), refreshToken.isExpired());
            return Optional.empty();
        }

        Optional<User> userOpt = userRepository.findById(refreshToken.getUserId());
        if (userOpt.isEmpty()) {
            LOGGER.error("User not found for refresh token - user ID: {}", refreshToken.getUserId());
            return Optional.empty();
        }

        User user = userOpt.get();

        refreshToken.markAsUsed();
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = tokenService.generateToken(user.getUsername());

        String newRefreshToken = createRefreshToken(user);

        LOGGER.info("Token refresh successful for user: {}", user.getUsername());

        return Optional.of(
            ImmutablePair.of(
                ImmutablePair.of(user, newAccessToken),
                newRefreshToken
            )
        );
    }

    @Override
    @Transactional
    public void handle(RevokeRefreshTokenCommand command) {
        LOGGER.info("Revoking refresh token");

        String tokenHash = hashToken(command.refreshToken());
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);

        if (refreshTokenOpt.isEmpty()) {
            LOGGER.warn("Refresh token not found for revocation");
            return;
        }

        RefreshToken refreshToken = refreshTokenOpt.get();
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        LOGGER.info("Refresh token revoked successfully");
    }

    @Override
    @Transactional
    public void handle(RevokeAllUserRefreshTokensCommand command) {
        LOGGER.info("Revoking all refresh tokens for user ID: {}", command.userId());
        revokeAllUserTokens(command.userId());
    }

    @Override
    @Transactional
    public void handle(SignOutCommand command) {
        LOGGER.info("Sign-out requested for user: {}", command.username());

        Optional<User> userOpt = userRepository.findByUsername(command.username());
        if (userOpt.isEmpty()) {
            LOGGER.warn("User not found for sign-out: {}", command.username());
            return;
        }

        User user = userOpt.get();
        revokeAllUserTokens(user.getId());

        LOGGER.info("Sign-out completed for user: {}", command.username());
    }

    private void revokeAllUserTokens(Long userId) {
        var tokens = refreshTokenRepository.findByUserIdAndRevoked(userId, false);
        tokens.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(tokens);
        LOGGER.info("Revoked {} tokens for user ID: {}", tokens.size(), userId);
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
