package com.finalproject.ecommerce.ecommerce.iam.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.iam.application.internal.outboundservices.hashing.HashingService;
import com.finalproject.ecommerce.ecommerce.iam.application.internal.outboundservices.tokens.TokenService;
import com.finalproject.ecommerce.ecommerce.iam.domain.exceptions.AccountNotActivatedException;
import com.finalproject.ecommerce.ecommerce.iam.domain.exceptions.InvalidActivationTokenException;
import com.finalproject.ecommerce.ecommerce.iam.domain.exceptions.InvalidPasswordResetTokenException;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.UserToken;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.valueobjects.Roles;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.valueobjects.TokenType;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.RefreshTokenCommandService;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.AccountActivationTokenRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.finalproject.ecommerce.ecommerce.notifications.interfaces.acl.NotificationContextFacade;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.RateLimitExceededException;
import com.finalproject.ecommerce.ecommerce.shared.infrastructure.ratelimit.RateLimiterService;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCommandServiceImpl")
class UserCommandServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private HashingService hashingService;
    @Mock private TokenService tokenService;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenCommandService refreshTokenCommandService;
    @Mock private AccountActivationTokenRepository accountActivationTokenRepository;
    @Mock private NotificationContextFacade notificationContextFacade;
    @Mock private RateLimiterService rateLimiterService;

    @InjectMocks private UserCommandServiceImpl service;

    private Role clientRole;
    private Role managerRole;
    private User activeUser;

    @BeforeEach
    void setUp() {
        clientRole = new Role(Roles.ROLE_CLIENT);
        managerRole = new Role(Roles.ROLE_MANAGER);
        activeUser = new User("testuser", "test@example.com", "encodedPass", clientRole);
        activeUser.activate();
    }

    @Nested
    @DisplayName("Sign Up")
    class SignUpTests {

        @Test
        @DisplayName("should sign up client with non-raven email")
        void shouldSignUpClient() {
            var cmd = new SignUpCommand("newuser", "newuser@gmail.com", "password123");
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("newuser@gmail.com")).thenReturn(false);
            when(roleRepository.findByName(Roles.ROLE_CLIENT)).thenReturn(Optional.of(clientRole));
            when(hashingService.encode(anyString())).thenReturn("encodedValue");
            when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(activeUser));

            Optional<User> result = service.handle(cmd);

            assertThat(result).isPresent();
            verify(userRepository).save(any(User.class));
            verify(accountActivationTokenRepository).save(any(UserToken.class));
        }

        @Test
        @DisplayName("should assign manager role for @ravn.co email")
        void shouldAssignManagerRoleForRavenEmail() {
            var cmd = new SignUpCommand("ravenuser", "admin@ravn.co", "password123");
            when(userRepository.existsByUsername("ravenuser")).thenReturn(false);
            when(userRepository.existsByEmail("admin@ravn.co")).thenReturn(false);
            when(roleRepository.findByName(Roles.ROLE_MANAGER)).thenReturn(Optional.of(managerRole));
            when(hashingService.encode(anyString())).thenReturn("encoded");
            when(userRepository.findByUsername("ravenuser")).thenReturn(Optional.of(
                    new User("ravenuser", "admin@ravn.co", "encoded", managerRole)));

            Optional<User> result = service.handle(cmd);

            assertThat(result).isPresent();
            verify(roleRepository).findByName(Roles.ROLE_MANAGER);
            verify(roleRepository, never()).findByName(Roles.ROLE_CLIENT);
        }

        @Test
        @DisplayName("should throw when username already exists")
        void shouldThrowWhenUsernameExists() {
            var cmd = new SignUpCommand("existing", "new@test.com", "pass12345");
            when(userRepository.existsByUsername("existing")).thenReturn(true);

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Username already exists");
        }

        @Test
        @DisplayName("should throw when email already exists")
        void shouldThrowWhenEmailExists() {
            var cmd = new SignUpCommand("newuser", "existing@test.com", "pass12345");
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Email already exists");
        }

        @Test
        @DisplayName("should encode password during sign up")
        void shouldEncodePassword() {
            var cmd = new SignUpCommand("user", "user@test.com", "rawPassword");
            when(userRepository.existsByUsername("user")).thenReturn(false);
            when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
            when(roleRepository.findByName(Roles.ROLE_CLIENT)).thenReturn(Optional.of(clientRole));
            when(hashingService.encode(anyString())).thenReturn("encodedValue");
            when(userRepository.findByUsername("user")).thenReturn(Optional.of(activeUser));

            service.handle(cmd);

            verify(hashingService).encode("rawPassword");
        }

        @Test
        @DisplayName("should generate activation token during sign up")
        void shouldGenerateActivationToken() {
            var cmd = new SignUpCommand("user", "user@test.com", "pass12345");
            when(userRepository.existsByUsername("user")).thenReturn(false);
            when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
            when(roleRepository.findByName(Roles.ROLE_CLIENT)).thenReturn(Optional.of(clientRole));
            when(hashingService.encode(anyString())).thenReturn("encoded");
            when(userRepository.findByUsername("user")).thenReturn(Optional.of(activeUser));

            service.handle(cmd);

            verify(accountActivationTokenRepository).save(any(UserToken.class));
            verify(notificationContextFacade).sendWelcomeEmail(eq("user@test.com"), eq("user"), anyString());
        }

        @Test
        @DisplayName("should create user as inactive by default")
        void shouldCreateInactiveUser() {
            var cmd = new SignUpCommand("user", "user@test.com", "pass12345");
            when(userRepository.existsByUsername("user")).thenReturn(false);
            when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
            when(roleRepository.findByName(Roles.ROLE_CLIENT)).thenReturn(Optional.of(clientRole));
            when(hashingService.encode(anyString())).thenReturn("encoded");
            when(userRepository.findByUsername("user")).thenReturn(Optional.of(activeUser));

            service.handle(cmd);

            verify(userRepository).save(argThat(user -> !user.getIsActive()));
        }
    }

    @Nested
    @DisplayName("Sign In")
    class SignInTests {

        @Test
        @DisplayName("should sign in activated user with correct credentials")
        void shouldSignIn() {
            var cmd = new SignInCommand("testuser", "rawPassword");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));
            when(hashingService.matches("rawPassword", "encodedPass")).thenReturn(true);
            when(tokenService.generateToken("testuser")).thenReturn("access_token_123");
            when(refreshTokenCommandService.createRefreshToken(activeUser)).thenReturn("refresh_token_456");

            var result = service.handle(cmd);

            assertThat(result).isPresent();
            ImmutablePair<ImmutablePair<User, String>, String> pair = result.get();
            assertThat(pair.getLeft().getLeft()).isEqualTo(activeUser);
            assertThat(pair.getLeft().getRight()).isEqualTo("access_token_123");
            assertThat(pair.getRight()).isEqualTo("refresh_token_456");
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            var cmd = new SignInCommand("unknown", "password");
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("should throw when account is not activated")
        void shouldThrowWhenNotActivated() {
            User inactiveUser = new User("inactive", "inactive@test.com", "pass", clientRole);
            var cmd = new SignInCommand("inactive", "password");
            when(userRepository.findByUsername("inactive")).thenReturn(Optional.of(inactiveUser));

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(AccountNotActivatedException.class)
                    .hasMessageContaining("not activated");
        }

        @Test
        @DisplayName("should throw when password is invalid")
        void shouldThrowWhenInvalidPassword() {
            var cmd = new SignInCommand("testuser", "wrongPassword");
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));
            when(hashingService.matches("wrongPassword", "encodedPass")).thenReturn(false);

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid password");
        }
    }

    @Nested
    @DisplayName("Activate Account")
    class ActivateAccountTests {

        @Test
        @DisplayName("should activate account with valid activation token")
        void shouldActivateAccount() {
            User inactiveUser = new User("user", "user@test.com", "pass", clientRole);
            var validToken = new UserToken(inactiveUser, "hashedToken", new Date(System.currentTimeMillis() + 86400000), TokenType.ACCOUNT_ACTIVATION);
            when(accountActivationTokenRepository.findByTokenTypeAndIsUsedFalse(TokenType.ACCOUNT_ACTIVATION))
                    .thenReturn(List.of(validToken));
            when(hashingService.matches(eq("raw_token"), eq("hashedToken"))).thenReturn(true);
            when(userRepository.findById(any())).thenReturn(Optional.of(inactiveUser));

            boolean result = service.handle(new ActivateAccountCommand("raw_token"));

            assertThat(result).isTrue();
            verify(userRepository).save(argThat(User::getIsActive));
        }

        @Test
        @DisplayName("should throw when token is invalid")
        void shouldThrowWhenTokenInvalid() {
            when(accountActivationTokenRepository.findByTokenTypeAndIsUsedFalse(TokenType.ACCOUNT_ACTIVATION))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> service.handle(new ActivateAccountCommand("invalid_token")))
                    .isInstanceOf(InvalidActivationTokenException.class);
        }

        @Test
        @DisplayName("should NOT activate account with a password reset token")
        void shouldNotActivateWithPasswordResetToken() {
            when(accountActivationTokenRepository.findByTokenTypeAndIsUsedFalse(TokenType.ACCOUNT_ACTIVATION))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> service.handle(new ActivateAccountCommand("password_reset_token")))
                    .isInstanceOf(InvalidActivationTokenException.class);
        }
    }

    @Nested
    @DisplayName("Resend Activation Token")
    class ResendActivationTests {

        @Test
        @DisplayName("should resend activation token for inactive user")
        void shouldResendToken() {
            User inactiveUser = new User("user", "user@test.com", "pass", clientRole);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(inactiveUser));
            when(accountActivationTokenRepository.findByUser_IdAndTokenTypeAndIsUsedFalse(any(), eq(TokenType.ACCOUNT_ACTIVATION)))
                    .thenReturn(List.of());
            when(hashingService.encode(anyString())).thenReturn("newHashedToken");

            boolean result = service.handle(new ResendActivationTokenCommand("user@test.com"));

            assertThat(result).isTrue();
            verify(accountActivationTokenRepository).save(any(UserToken.class));
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new ResendActivationTokenCommand("unknown@test.com")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User with email not found");
        }

        @Test
        @DisplayName("should throw when account is already activated")
        void shouldThrowWhenAlreadyActivated() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));

            assertThatThrownBy(() -> service.handle(new ResendActivationTokenCommand("test@example.com")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("already activated");
        }
    }

    @Nested
    @DisplayName("Forgot Password")
    class ForgotPasswordTests {

        @Test
        @DisplayName("should generate password reset token")
        void shouldGenerateResetToken() {
            when(rateLimiterService.isAllowed("test@example.com", "forgot-password")).thenReturn(true);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
            when(accountActivationTokenRepository.findByUser_IdAndTokenTypeAndIsUsedFalse(any(), eq(TokenType.PASSWORD_RESET)))
                    .thenReturn(List.of());
            when(hashingService.encode(anyString())).thenReturn("hashedResetToken");

            boolean result = service.handle(new ForgotPasswordCommand("test@example.com"));

            assertThat(result).isTrue();
            verify(accountActivationTokenRepository).save(any(UserToken.class));
            verify(notificationContextFacade).sendPasswordResetEmail(eq("test@example.com"), eq("testuser"), anyString(), eq(15));
        }

        @Test
        @DisplayName("should throw when rate limited")
        void shouldThrowWhenRateLimited() {
            when(rateLimiterService.isAllowed("test@example.com", "forgot-password")).thenReturn(false);
            when(rateLimiterService.getSecondsUntilReset("test@example.com", "forgot-password")).thenReturn(300L);

            assertThatThrownBy(() -> service.handle(new ForgotPasswordCommand("test@example.com")))
                    .isInstanceOf(RateLimitExceededException.class)
                    .hasMessageContaining("Too many password reset requests");
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(rateLimiterService.isAllowed("unknown@test.com", "forgot-password")).thenReturn(true);
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new ForgotPasswordCommand("unknown@test.com")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User with email not found");
        }
    }

    @Nested
    @DisplayName("Reset Password")
    class ResetPasswordTests {

        @Test
        @DisplayName("should reset password with valid token")
        void shouldResetPassword() {
            var validToken = new UserToken(activeUser, "hashedToken", new Date(System.currentTimeMillis() + 86400000), TokenType.PASSWORD_RESET);
            when(accountActivationTokenRepository.findByTokenTypeAndIsUsedFalse(TokenType.PASSWORD_RESET))
                    .thenReturn(List.of(validToken));
            when(hashingService.matches(eq("reset_token"), eq("hashedToken"))).thenReturn(true);
            when(userRepository.findById(any())).thenReturn(Optional.of(activeUser));
            when(hashingService.encode("newPassword")).thenReturn("encodedNewPassword");
            when(rateLimiterService.isAllowed(anyString(), eq("reset-password"))).thenReturn(true);

            boolean result = service.handle(new ResetPasswordCommand("reset_token", "newPassword", "newPassword"));

            assertThat(result).isTrue();
            verify(userRepository).save(activeUser);
            verify(hashingService).encode("newPassword");
        }

        @Test
        @DisplayName("should throw when passwords do not match")
        void shouldThrowWhenPasswordsMismatch() {
            var cmd = new ResetPasswordCommand("token", "pass1", "pass2");

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidPasswordResetTokenException.class)
                    .hasMessageContaining("Passwords do not match");
        }

        @Test
        @DisplayName("should throw when token is invalid")
        void shouldThrowWhenTokenInvalid() {
            when(accountActivationTokenRepository.findByTokenTypeAndIsUsedFalse(TokenType.PASSWORD_RESET))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> service.handle(new ResetPasswordCommand("bad_token", "pass", "pass")))
                    .isInstanceOf(InvalidPasswordResetTokenException.class);
        }

        @Test
        @DisplayName("should throw when rate limited for reset-password")
        void shouldThrowWhenRateLimited() {
            var validToken = new UserToken(activeUser, "hashedToken", new Date(System.currentTimeMillis() + 86400000), TokenType.PASSWORD_RESET);
            when(accountActivationTokenRepository.findByTokenTypeAndIsUsedFalse(TokenType.PASSWORD_RESET))
                    .thenReturn(List.of(validToken));
            when(hashingService.matches(eq("token"), eq("hashedToken"))).thenReturn(true);
            when(userRepository.findById(any())).thenReturn(Optional.of(activeUser));
            when(rateLimiterService.isAllowed(anyString(), eq("reset-password"))).thenReturn(false);
            when(rateLimiterService.getSecondsUntilReset(anyString(), eq("reset-password"))).thenReturn(60L);

            assertThatThrownBy(() -> service.handle(new ResetPasswordCommand("token", "pass", "pass")))
                    .isInstanceOf(RateLimitExceededException.class);
        }

        @Test
        @DisplayName("should NOT reset password with an activation token")
        void shouldNotResetWithActivationToken() {
            when(accountActivationTokenRepository.findByTokenTypeAndIsUsedFalse(TokenType.PASSWORD_RESET))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> service.handle(new ResetPasswordCommand("activation_token", "pass", "pass")))
                    .isInstanceOf(InvalidPasswordResetTokenException.class);
        }
    }

    @Nested
    @DisplayName("Update User")
    class UpdateUserTests {

        @Test
        @DisplayName("should update user successfully")
        void shouldUpdateUser() {
            var cmd = new UpdateUserCommand(1L, "updated", "updated@test.com", "newpass", clientRole);
            when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));
            when(userRepository.existsByUsernameAndIdIsNot("updated", 1L)).thenReturn(false);
            when(userRepository.existsByEmailAndIdIsNot("updated@test.com", 1L)).thenReturn(false);
            when(roleRepository.findByName(Roles.ROLE_CLIENT)).thenReturn(Optional.of(clientRole));
            when(hashingService.encode("newpass")).thenReturn("encodedNewPass");
            when(userRepository.save(any(User.class))).thenReturn(activeUser);

            Optional<User> result = service.handle(cmd);

            assertThat(result).isPresent();
            verify(hashingService).encode("newpass");
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            var cmd = new UpdateUserCommand(99L, "user", "user@test.com", "pass", clientRole);
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("should throw when username conflicts with another user")
        void shouldThrowWhenUsernameConflicts() {
            var cmd = new UpdateUserCommand(1L, "taken", "new@test.com", "pass", clientRole);
            when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));
            when(userRepository.existsByUsernameAndIdIsNot("taken", 1L)).thenReturn(true);

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("should throw when email conflicts with another user")
        void shouldThrowWhenEmailConflicts() {
            var cmd = new UpdateUserCommand(1L, "user", "taken@test.com", "pass", clientRole);
            when(userRepository.findById(1L)).thenReturn(Optional.of(activeUser));
            when(userRepository.existsByUsernameAndIdIsNot("user", 1L)).thenReturn(false);
            when(userRepository.existsByEmailAndIdIsNot("taken@test.com", 1L)).thenReturn(true);

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("already exists");
        }
    }

    @Nested
    @DisplayName("Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("should delete existing user")
        void shouldDeleteUser() {
            when(userRepository.existsById(1L)).thenReturn(true);

            service.handle(new DeleteUserCommand(1L));

            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.handle(new DeleteUserCommand(99L)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not found");
        }
    }
}






