package com.finalproject.ecommerce.ecommerce.iam.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.Date;

@Entity
@Getter
public class UserToken extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true, length = 500)
    private String hashedToken;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @NotNull
    @Column(nullable = false)
    private Date expiresAt;

    @NotNull
    @Column(nullable = false)
    private Boolean isUsed;


    protected UserToken() {
        this.isUsed = false;
    }

    public UserToken(User user, String hashedToken, Date expiresAt) {
        this();
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (hashedToken == null || hashedToken.isBlank()) {
            throw new IllegalArgumentException("Hashed token cannot be null or empty");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("Expiration date cannot be null");
        }
        this.user = user;
        this.hashedToken = hashedToken;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return new Date().after(this.expiresAt);
    }

    public boolean isValid() {
        return !isUsed && !isExpired();
    }

    public void markAsUsed() {
        if (this.isUsed) {
            throw new IllegalStateException("Token has already been used");
        }
        if (isExpired()) {
            throw new IllegalStateException("Cannot use an expired token");
        }
        this.isUsed = true;
    }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
}

