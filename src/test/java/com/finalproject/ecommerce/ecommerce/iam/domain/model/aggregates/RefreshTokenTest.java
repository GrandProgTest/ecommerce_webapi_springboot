package com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.valueobjects.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RefreshToken Aggregate")
class RefreshTokenTest {

    private User user;
    private Instant futureExpiry;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@example.com", "pass12345", new Role(Roles.ROLE_CLIENT));
        futureExpiry = Instant.now().plus(7, ChronoUnit.DAYS);
    }

    @Nested
    @DisplayName("Creation")
    class CreationTests {
        @Test
        @DisplayName("should create refresh token with valid fields")
        void shouldCreateRefreshToken() {
            RefreshToken token = new RefreshToken("hash123", user, futureExpiry);

            assertThat(token.getTokenHash()).isEqualTo("hash123");
            assertThat(token.getUser()).isEqualTo(user);
            assertThat(token.getExpiresAt()).isEqualTo(futureExpiry);
            assertThat(token.isRevoked()).isFalse();
            assertThat(token.isUsed()).isFalse();
        }

        @Test
        @DisplayName("should throw when token hash is null")
        void shouldThrowWhenHashNull() {
            assertThatThrownBy(() -> new RefreshToken(null, user, futureExpiry))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Token hash cannot be null");
        }

        @Test
        @DisplayName("should throw when token hash is blank")
        void shouldThrowWhenHashBlank() {
            assertThatThrownBy(() -> new RefreshToken("   ", user, futureExpiry))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Token hash cannot be null");
        }

        @Test
        @DisplayName("should throw when user is null")
        void shouldThrowWhenUserNull() {
            assertThatThrownBy(() -> new RefreshToken("hash", null, futureExpiry))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User cannot be null");
        }

        @Test
        @DisplayName("should throw when expiration is in the past")
        void shouldThrowWhenExpiredDate() {
            Instant past = Instant.now().minus(1, ChronoUnit.DAYS);

            assertThatThrownBy(() -> new RefreshToken("hash", user, past))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expiration date must be in the future");
        }

        @Test
        @DisplayName("should throw when expiration is null")
        void shouldThrowWhenExpirationNull() {
            assertThatThrownBy(() -> new RefreshToken("hash", user, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expiration date must be in the future");
        }
    }

    @Nested
    @DisplayName("Validity")
    class ValidityTests {
        @Test
        @DisplayName("should be valid when not expired, not revoked, not used")
        void shouldBeValid() {
            RefreshToken token = new RefreshToken("hash", user, futureExpiry);

            assertThat(token.isValid()).isTrue();
        }

        @Test
        @DisplayName("should be invalid after revocation")
        void shouldBeInvalidAfterRevocation() {
            RefreshToken token = new RefreshToken("hash", user, futureExpiry);
            token.revoke();

            assertThat(token.isValid()).isFalse();
        }

        @Test
        @DisplayName("should be invalid after use")
        void shouldBeInvalidAfterUse() {
            RefreshToken token = new RefreshToken("hash", user, futureExpiry);
            token.markAsUsed();

            assertThat(token.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Mark As Used")
    class MarkAsUsedTests {
        @Test
        @DisplayName("should mark as used successfully")
        void shouldMarkAsUsed() {
            RefreshToken token = new RefreshToken("hash", user, futureExpiry);

            token.markAsUsed();

            assertThat(token.isUsed()).isTrue();
            assertThat(token.getUsedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw when already used (replay attack)")
        void shouldThrowWhenAlreadyUsed() {
            RefreshToken token = new RefreshToken("hash", user, futureExpiry);
            token.markAsUsed();

            assertThatThrownBy(token::markAsUsed)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already been used");
        }

        @Test
        @DisplayName("should throw when revoked")
        void shouldThrowWhenRevoked() {
            RefreshToken token = new RefreshToken("hash", user, futureExpiry);
            token.revoke();

            assertThatThrownBy(token::markAsUsed)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("revoked token");
        }
    }

    @Nested
    @DisplayName("Revoke")
    class RevokeTests {
        @Test
        @DisplayName("should revoke token successfully")
        void shouldRevoke() {
            RefreshToken token = new RefreshToken("hash", user, futureExpiry);

            token.revoke();

            assertThat(token.isRevoked()).isTrue();
        }

        @Test
        @DisplayName("should throw when already revoked")
        void shouldThrowWhenAlreadyRevoked() {
            RefreshToken token = new RefreshToken("hash", user, futureExpiry);
            token.revoke();

            assertThatThrownBy(token::revoke)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already revoked");
        }
    }

    @Nested
    @DisplayName("Belongs To User")
    class BelongsToUserTests {
        @Test
        @DisplayName("should return true for same user")
        void shouldBelongToUser() {
            RefreshToken token = new RefreshToken("hash", user, futureExpiry);

            assertThat(token.belongsToUser(user)).isTrue();
        }

        @Test
        @DisplayName("should return false for different user")
        void shouldNotBelongToDifferentUser() {
            RefreshToken token = new RefreshToken("hash", user, futureExpiry);
            User other = new User("other", "other@example.com", "pass12345", new Role(Roles.ROLE_CLIENT));

            assertThat(token.belongsToUser(other)).isFalse();
        }
    }
}

