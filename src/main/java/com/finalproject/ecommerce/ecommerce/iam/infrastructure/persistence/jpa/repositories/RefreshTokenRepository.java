package com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;


@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {


    Optional<RefreshToken> findByTokenHash(String tokenHash);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findWithLockByTokenHash(String tokenHash);
    List<RefreshToken> findByUserId(Long userId);

    List<RefreshToken> findByUserIdAndRevokedAndUsedAndExpiresAtAfter(
            Long userId,
            boolean revoked,
            boolean used,
            Instant expiresAt
    );

    int deleteByExpiresAtBefore(Instant expiresAt);
    void deleteByUserId(Long userId);
    boolean existsByTokenHash(String tokenHash);
}
