package com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates;

import com.finalproject.ecommerce.ecommerce.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_token_hash", columnList = "tokenHash"),
    @Index(name = "idx_user_id", columnList = "userId")
})
@Getter
public class RefreshToken extends AuditableAbstractAggregateRoot<RefreshToken> {

    @NotBlank
    @Column(nullable = false, unique = true, length = 255)
    private String tokenHash;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Long userId;

    @NotNull
    @Future
    @Column(nullable = false)
    private Instant expiresAt;

    @NotNull
    @Column(nullable = false)
    private boolean revoked;

    @NotNull
    @Column(nullable = false)
    private boolean used;

    @Column
    private Instant usedAt;

    public RefreshToken() {}

    public RefreshToken(String tokenHash, Long userId, Instant expiresAt) {
        validateInvariants(tokenHash, userId, expiresAt);

        this.tokenHash = tokenHash;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.revoked = false;
        this.used = false;
    }


    private void validateInvariants(String tokenHash, Long userId, Instant expiresAt) {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("Token hash cannot be null or empty");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }
        if (expiresAt == null || expiresAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Expiration date must be in the future");
        }
    }

    public boolean isValid() {
        return !isExpired() && !revoked && !used;
    }


    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }


    public void markAsUsed() {
        if (this.used) {
            throw new IllegalStateException("Token has already been used - possible replay attack");
        }
        if (this.revoked) {
            throw new IllegalStateException("Cannot use a revoked token");
        }
        if (this.isExpired()) {
            throw new IllegalStateException("Cannot use an expired token");
        }

        this.used = true;
        this.usedAt = Instant.now();
    }

    public void revoke() {
        if (this.revoked) {
            throw new IllegalStateException("Token is already revoked");
        }
        this.revoked = true;
    }

    public boolean belongsToUser(Long userId) {
        return this.userId.equals(userId);
    }
}
