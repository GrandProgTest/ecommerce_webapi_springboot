package com.finalproject.ecommerce.ecommerce.iam.application.internal.eventhandlers;

import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RefreshTokenCleanupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshTokenCleanupService.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // Reconsider cleanup service

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        LOGGER.info("Starting cleanup of expired refresh tokens");

        try {
            int deletedCount = refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
            LOGGER.info("Cleanup completed: {} expired tokens removed", deletedCount);
        } catch (Exception e) {
            LOGGER.error("Error during token cleanup: {}", e.getMessage(), e);
        }
    }
}
