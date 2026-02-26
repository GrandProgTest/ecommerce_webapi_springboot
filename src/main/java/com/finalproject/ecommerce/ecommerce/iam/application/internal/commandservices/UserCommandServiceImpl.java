package com.finalproject.ecommerce.ecommerce.iam.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.iam.application.internal.outboundservices.hashing.HashingService;
import com.finalproject.ecommerce.ecommerce.iam.application.internal.outboundservices.tokens.TokenService;
import com.finalproject.ecommerce.ecommerce.iam.domain.exceptions.AccountNotActivatedException;
import com.finalproject.ecommerce.ecommerce.iam.domain.exceptions.InvalidActivationTokenException;
import com.finalproject.ecommerce.ecommerce.iam.domain.exceptions.InvalidPasswordResetTokenException;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ActivateAccountCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.DeleteUserCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ForgotPasswordCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ResendActivationTokenCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ResetPasswordCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignInCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignUpCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.UpdateUserCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.UserToken;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.validators.RavenEmailValidator;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.valueobjects.TokenType;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.RefreshTokenCommandService;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserCommandService;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.AccountActivationTokenRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.finalproject.ecommerce.ecommerce.notifications.interfaces.acl.NotificationContextFacade;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.RateLimitExceededException;
import com.finalproject.ecommerce.ecommerce.shared.infrastructure.ratelimit.RateLimiterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;
    private final RefreshTokenCommandService refreshTokenCommandService;
    private final AccountActivationTokenRepository accountActivationTokenRepository;
    private final NotificationContextFacade notificationContextFacade;
    private final RateLimiterService rateLimiterService;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            HashingService hashingService,
            TokenService tokenService,
            RoleRepository roleRepository,
            RefreshTokenCommandService refreshTokenCommandService,
            AccountActivationTokenRepository accountActivationTokenRepository,
            NotificationContextFacade notificationContextFacade,
            RateLimiterService rateLimiterService) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
        this.refreshTokenCommandService = refreshTokenCommandService;
        this.accountActivationTokenRepository = accountActivationTokenRepository;
        this.notificationContextFacade = notificationContextFacade;
        this.rateLimiterService = rateLimiterService;
    }


    @Override
    public Optional<ImmutablePair<ImmutablePair<User, String>, String>> handle(SignInCommand command) {
        var user = userRepository.findByUsername(command.username());
        if (user.isEmpty()) throw new RuntimeException("User not found");

        if (!user.get().getIsActive()) {
            throw new AccountNotActivatedException(command.username());
        }

        if (!hashingService.matches(command.password(), user.get().getPassword()))
            throw new RuntimeException("Invalid password");

        var accessToken = tokenService.generateToken(user.get().getUsername());

        var refreshToken = refreshTokenCommandService.createRefreshToken(user.get());

        return Optional.of(ImmutablePair.of(ImmutablePair.of(user.get(), accessToken), refreshToken));
    }


    @Override
    @Transactional
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username()))
            throw new RuntimeException("Username already exists");

        if (userRepository.existsByEmail(command.email()))
            throw new RuntimeException("Email already exists");

        Role role;
        if (RavenEmailValidator.isRavenEmail(command.email())) {
            role = roleRepository.findByName(Role.getDefaultManagerRole().getName())
                    .orElseThrow(() -> new RuntimeException("Manager role not found"));
        } else {
            role = roleRepository.findByName(Role.getDefaultClientRole().getName())
                    .orElseThrow(() -> new RuntimeException("Client role not found"));
        }

        var user = new User(
                command.username(),
                command.email(),
                hashingService.encode(command.password()),
                role
        );

        userRepository.save(user);

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = hashingService.encode(rawToken);
        Date expiresAt = Date.from(Instant.now().plus(24, ChronoUnit.HOURS));

        var activationToken = new UserToken(user, hashedToken, expiresAt, TokenType.ACCOUNT_ACTIVATION);
        accountActivationTokenRepository.save(activationToken);

        sendActivationEmail(user.getEmail(), user.getUsername(), rawToken);

        return userRepository.findByUsername(command.username());
    }

    private void sendActivationEmail(String email, String username, String rawActivationToken) {
        try {
            String activationLink = "http://localhost:8080/api/v1/auth/activate?token=" + rawActivationToken;

            notificationContextFacade.sendWelcomeEmail(email, username, activationLink);
        } catch (Exception e) {
            log.error("Failed to send activation email to {}: {}", email, e.getMessage());
        }
    }

    @Override
    public Optional<User> handle(UpdateUserCommand command) {
        var user = userRepository.findById(command.userId()).orElseThrow(() -> new RuntimeException("User with id %s not found".formatted(command.userId())));

        if (userRepository.existsByUsernameAndIdIsNot(command.username(), command.userId()))
            throw new RuntimeException("User with username %s already exists".formatted(command.username()));

        if (userRepository.existsByEmailAndIdIsNot(command.email(), command.userId()))
            throw new RuntimeException("User with email %s already exists".formatted(command.email()));

        var roleName = command.role().getName();
        var role = roleRepository.findByName(roleName).orElseThrow(() -> new RuntimeException("Role with name %s not found".formatted(roleName)));

        user.setUsername(command.username());
        user.setEmail(command.email());
        user.setPassword(hashingService.encode(command.password()));
        user.setRole(role);

        try {
            return Optional.of(userRepository.save(user));
        } catch (Exception e) {
            throw new RuntimeException("Error while updating user: %s".formatted(e.getMessage()));
        }
    }


    @Override
    public void handle(DeleteUserCommand command) {
        if (!userRepository.existsById(command.userId()))
            throw new RuntimeException("User with id %s not found".formatted(command.userId()));

        try {
            userRepository.deleteById(command.userId());
        } catch (Exception e) {
            throw new RuntimeException("Error while deleting user: %s".formatted(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public boolean handle(ActivateAccountCommand command) {
        try {
            String rawToken = command.activationToken();

            var potentialToken = accountActivationTokenRepository
                    .findByTokenTypeAndIsUsedFalse(TokenType.ACCOUNT_ACTIVATION).stream()
                    .filter(token -> !token.isExpired())
                    .filter(token -> hashingService.matches(rawToken, token.getHashedToken()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidActivationTokenException("Token not found or invalid"));

            var user = userRepository.findById(potentialToken.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.activate();
            userRepository.save(user);

            potentialToken.markAsUsed();
            accountActivationTokenRepository.save(potentialToken);

            log.info("Account activated successfully for user: {}", user.getUsername());

            return true;
        } catch (InvalidActivationTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error activating account: {}", e.getMessage(), e);
            throw new InvalidActivationTokenException("Invalid token: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean handle(ResendActivationTokenCommand command) {
        var user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new RuntimeException("User with email not found"));

        if (user.getIsActive()) {
            throw new RuntimeException("Account is already activated");
        }

        var existingTokens = accountActivationTokenRepository
                .findByUser_IdAndTokenTypeAndIsUsedFalse(user.getId(), TokenType.ACCOUNT_ACTIVATION);

        existingTokens.forEach(token -> {
            token.forceMarkAsUsed();
            accountActivationTokenRepository.save(token);
            log.info("Previous activation token invalidated for user: {}", user.getUsername());
        });

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = hashingService.encode(rawToken);
        Date expiresAt = Date.from(Instant.now().plus(24, ChronoUnit.HOURS));

        var activationToken = new UserToken(user, hashedToken, expiresAt, TokenType.ACCOUNT_ACTIVATION);
        accountActivationTokenRepository.save(activationToken);

        log.info("New activation token generated for user: {}", user.getUsername());

        sendActivationEmail(user.getEmail(), user.getUsername(), rawToken);

        return true;
    }

    @Override
    public boolean handle(ForgotPasswordCommand command) {
        if (!rateLimiterService.isAllowed(command.email(), "forgot-password")) {
            long retryAfter = rateLimiterService.getSecondsUntilReset(command.email(), "forgot-password");
            throw new RateLimitExceededException(
                    "Too many password reset requests. Please try again later.",
                    retryAfter
            );
        }

        var user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new RuntimeException("User with email not found"));

        var existingTokens = accountActivationTokenRepository
                .findByUser_IdAndTokenTypeAndIsUsedFalse(user.getId(), TokenType.PASSWORD_RESET);
        existingTokens.forEach(token -> {
            token.forceMarkAsUsed();
            accountActivationTokenRepository.save(token);
            log.info("Previous password reset token invalidated for user: {}", user.getUsername());
        });

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = hashingService.encode(rawToken);
        Date expiresAt = Date.from(Instant.now().plus(15, ChronoUnit.MINUTES));

        var resetToken = new UserToken(user, hashedToken, expiresAt, TokenType.PASSWORD_RESET);
        accountActivationTokenRepository.save(resetToken);

        log.info("Password reset token generated for user: {}", user.getUsername());

        sendPasswordResetEmail(user.getEmail(), user.getUsername(), rawToken);

        return true;
    }

    @Override
    @Transactional
    public boolean handle(ResetPasswordCommand command) {
        if (!command.password().equals(command.passwordConfirmation())) {
            throw new InvalidPasswordResetTokenException("Passwords do not match");
        }

        try {
            String rawToken = command.token();

            var potentialToken = accountActivationTokenRepository.findByTokenTypeAndIsUsedFalse(TokenType.PASSWORD_RESET).stream()
                    .filter(token -> !token.isExpired())
                    .filter(token -> hashingService.matches(rawToken, token.getHashedToken()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidPasswordResetTokenException("Invalid or expired reset token"));

            var user = userRepository.findById(potentialToken.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!rateLimiterService.isAllowed(user.getEmail(), "reset-password")) {
                long retryAfter = rateLimiterService.getSecondsUntilReset(user.getEmail(), "reset-password");
                throw new RateLimitExceededException(
                        "Too many password reset attempts. Please try again later.",
                        retryAfter
                );
            }

            user.setPassword(hashingService.encode(command.password()));
            userRepository.save(user);

            potentialToken.markAsUsed();
            accountActivationTokenRepository.save(potentialToken);

            log.info("Password reset successfully for user: {}", user.getUsername());

            sendPasswordChangedEmail(user.getEmail(), user.getUsername());

            return true;
        } catch (InvalidPasswordResetTokenException | RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error resetting password: {}", e.getMessage(), e);
            throw new InvalidPasswordResetTokenException("Failed to reset password: " + e.getMessage());
        }
    }

    private void sendPasswordResetEmail(String email, String username, String rawResetToken) {
        try {
            int expirationMinutes = 15;

            notificationContextFacade.sendPasswordResetEmail(email, username, rawResetToken, expirationMinutes);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
        }
    }

    private void sendPasswordChangedEmail(String email, String username) {
        try {
            String changeDateTime = new Date().toString();

            notificationContextFacade.sendPasswordChangedEmail(email, username, changeDateTime);
        } catch (Exception e) {
            log.error("Failed to send password changed email to {}: {}", email, e.getMessage());
        }
    }
}