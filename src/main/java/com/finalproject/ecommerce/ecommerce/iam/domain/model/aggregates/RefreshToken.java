package com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates;

import com.finalproject.ecommerce.ecommerce.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.time.Instant;

@Entity
@Getter
public class RefreshToken extends AuditableAbstractAggregateRoot<RefreshToken> {

    @NotBlank
    @Column(nullable = false, unique = true, length = 255)
    private String tokenHash;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private User user;

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

    public RefreshToken() {
    }

    public RefreshToken(String tokenHash, User user, Instant expiresAt) {
        validateInvariants(tokenHash, user, expiresAt);

        this.tokenHash = tokenHash;
        this.user = user;
        this.expiresAt = expiresAt;
        this.revoked = false;
        this.used = false;
    }


    private void validateInvariants(String tokenHash, User user, Instant expiresAt) {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("Token hash cannot be null or empty");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
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

    public boolean belongsToUser(User user) {
        return this.user.equals(user);
    }

    public Long getUserId() {
        return this.user != null ? this.user.getId() : null;
    }
}
