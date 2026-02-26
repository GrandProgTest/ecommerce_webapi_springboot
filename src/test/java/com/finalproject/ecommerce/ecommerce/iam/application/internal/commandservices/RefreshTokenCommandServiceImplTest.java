package com.finalproject.ecommerce.ecommerce.iam.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.iam.application.internal.outboundservices.tokens.TokenService;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.RefreshToken;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.RefreshTokenCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.RevokeAllUserRefreshTokensCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.RevokeRefreshTokenCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignOutCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.valueobjects.Roles;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.RefreshTokenRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration.properties.JwtProperties;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenCommandServiceImpl")
class RefreshTokenCommandServiceImplTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private TokenService tokenService;
    @Mock private JwtProperties jwtProperties;

    @InjectMocks private RefreshTokenCommandServiceImpl service;

    private User user;
    private JwtProperties.RefreshToken refreshTokenProps;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "test@example.com", "pass12345", new Role(Roles.ROLE_CLIENT));
        refreshTokenProps = new JwtProperties.RefreshToken();
        refreshTokenProps.setExpirationDays(7);
    }

    @Nested
    @DisplayName("Create Refresh Token")
    class CreateRefreshTokenTests {

        @Test
        @DisplayName("should create refresh token and revoke previous ones")
        void shouldCreateToken() {
            when(refreshTokenRepository.findByUserAndRevoked(user, false)).thenReturn(List.of());
            when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenProps);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

            String plainToken = service.createRefreshToken(user);

            assertThat(plainToken).isNotNull().isNotBlank();
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("should revoke existing tokens before creating new one")
        void shouldRevokeExisting() {
            RefreshToken existingToken = new RefreshToken("oldHash", user, Instant.now().plus(7, ChronoUnit.DAYS));
            when(refreshTokenRepository.findByUserAndRevoked(user, false))
                    .thenReturn(List.of(existingToken))
                    .thenReturn(List.of());
            when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenProps);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

            service.createRefreshToken(user);

            assertThat(existingToken.isRevoked()).isTrue();
            verify(refreshTokenRepository).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("Handle Refresh Token Command")
    class HandleRefreshTests {

        @Test
        @DisplayName("should return empty when token not found")
        void shouldReturnEmptyWhenNotFound() {
            when(refreshTokenRepository.findWithLockByTokenHash(anyString())).thenReturn(Optional.empty());

            var result = service.handle(new RefreshTokenCommand("some_token"));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should throw SecurityException on token reuse")
        void shouldThrowOnReuse() {
            RefreshToken usedToken = new RefreshToken("hash", user, Instant.now().plus(7, ChronoUnit.DAYS));
            usedToken.markAsUsed();
            when(refreshTokenRepository.findWithLockByTokenHash(anyString())).thenReturn(Optional.of(usedToken));
            when(refreshTokenRepository.findByUserAndRevoked(user, false)).thenReturn(List.of());

            assertThatThrownBy(() -> service.handle(new RefreshTokenCommand("reused_token")))
                    .isInstanceOf(SecurityException.class)
                    .hasMessageContaining("Token reuse detected");
        }

        @Test
        @DisplayName("should return empty when token is revoked")
        void shouldReturnEmptyWhenRevoked() {
            RefreshToken revokedToken = new RefreshToken("hash", user, Instant.now().plus(7, ChronoUnit.DAYS));
            revokedToken.revoke();
            when(refreshTokenRepository.findWithLockByTokenHash(anyString())).thenReturn(Optional.of(revokedToken));

            var result = service.handle(new RefreshTokenCommand("revoked_token"));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should rotate tokens successfully")
        void shouldRotateTokens() {
            RefreshToken validToken = new RefreshToken("hash", user, Instant.now().plus(7, ChronoUnit.DAYS));
            when(refreshTokenRepository.findWithLockByTokenHash(anyString())).thenReturn(Optional.of(validToken));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
            when(tokenService.generateToken("testuser")).thenReturn("new_access_token");
            when(refreshTokenRepository.findByUserAndRevoked(user, false)).thenReturn(List.of());
            when(jwtProperties.getRefreshToken()).thenReturn(refreshTokenProps);

            var result = service.handle(new RefreshTokenCommand("valid_token"));

            assertThat(result).isPresent();
            ImmutablePair<ImmutablePair<User, String>, String> pair = result.get();
            assertThat(pair.getLeft().getRight()).isEqualTo("new_access_token");
            assertThat(validToken.isUsed()).isTrue();
        }
    }

    @Nested
    @DisplayName("Revoke Refresh Token")
    class RevokeTokenTests {

        @Test
        @DisplayName("should revoke existing token")
        void shouldRevokeToken() {
            RefreshToken token = new RefreshToken("hash", user, Instant.now().plus(7, ChronoUnit.DAYS));
            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

            service.handle(new RevokeRefreshTokenCommand("some_token"));

            assertThat(token.isRevoked()).isTrue();
            verify(refreshTokenRepository).save(token);
        }

        @Test
        @DisplayName("should do nothing when token not found")
        void shouldDoNothingWhenNotFound() {
            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

            service.handle(new RevokeRefreshTokenCommand("unknown_token"));

            verify(refreshTokenRepository, never()).save(any());
        }
    }


    @Nested
    @DisplayName("Revoke All User Refresh Tokens")
    class RevokeAllTests {

        @Test
        @DisplayName("should revoke all tokens for user")
        void shouldRevokeAll() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            RefreshToken t1 = new RefreshToken("h1", user, Instant.now().plus(7, ChronoUnit.DAYS));
            RefreshToken t2 = new RefreshToken("h2", user, Instant.now().plus(7, ChronoUnit.DAYS));
            when(refreshTokenRepository.findByUserAndRevoked(user, false)).thenReturn(List.of(t1, t2));

            service.handle(new RevokeAllUserRefreshTokensCommand(1L));

            assertThat(t1.isRevoked()).isTrue();
            assertThat(t2.isRevoked()).isTrue();
            verify(refreshTokenRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("should do nothing when user not found")
        void shouldDoNothingWhenUserNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            service.handle(new RevokeAllUserRefreshTokensCommand(99L));

            verify(refreshTokenRepository, never()).findByUserAndRevoked(any(), anyBoolean());
        }
    }

    @Nested
    @DisplayName("Sign Out")
    class SignOutTests {

        @Test
        @DisplayName("should revoke all tokens on sign out")
        void shouldRevokeAllOnSignOut() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(refreshTokenRepository.findByUserAndRevoked(user, false)).thenReturn(List.of());

            service.handle(new SignOutCommand("testuser"));

            verify(refreshTokenRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("should do nothing when user not found on sign out")
        void shouldDoNothingWhenUserNotFound() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            service.handle(new SignOutCommand("unknown"));

            verify(refreshTokenRepository, never()).findByUserAndRevoked(any(), anyBoolean());
        }
    }
}

