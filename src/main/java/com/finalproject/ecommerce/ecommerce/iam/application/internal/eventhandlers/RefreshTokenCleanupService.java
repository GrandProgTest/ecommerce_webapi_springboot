package com.finalproject.ecommerce.ecommerce.iam.application.internal.eventhandlers;

import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
public class RefreshTokenCleanupService {


    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // Reconsider cleanup service

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens");

        try {
            int deletedCount = refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
            log.info("Cleanup completed: {} expired tokens removed", deletedCount);
        } catch (Exception e) {
            log.error("Error during token cleanup: {}", e.getMessage(), e);
        }
    }
}
