package com.finalproject.ecommerce.ecommerce.shared.infrastructure.ratelimit;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RateLimiterService {

    private final ConcurrentHashMap<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    private final RateLimiterRegistry rateLimiterRegistry;

    public RateLimiterService() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(10)
                .limitRefreshPeriod(Duration.ofHours(1))
                .timeoutDuration(Duration.ofMillis(100))
                .build();

        this.rateLimiterRegistry = RateLimiterRegistry.of(config);
    }

    public boolean isAllowed(String key, String operation) {
        String rateLimiterKey = operation + ":" + key;

        RateLimiter rateLimiter = rateLimiters.computeIfAbsent(
                rateLimiterKey,
                k -> rateLimiterRegistry.rateLimiter(k)
        );

        boolean allowed = rateLimiter.acquirePermission();

        if (!allowed) {
            log.warn("Rate limit exceeded for operation '{}' and key '{}'", operation, key);
        }

        return allowed;
    }

    public int getRemainingAttempts(String key, String operation) {
        String rateLimiterKey = operation + ":" + key;
        RateLimiter rateLimiter = rateLimiters.get(rateLimiterKey);

        if (rateLimiter == null) {
            return 10;
        }

        return rateLimiter.getMetrics().getAvailablePermissions();
    }


    public long getSecondsUntilReset(String key, String operation) {
        return 3600;
    }
}

