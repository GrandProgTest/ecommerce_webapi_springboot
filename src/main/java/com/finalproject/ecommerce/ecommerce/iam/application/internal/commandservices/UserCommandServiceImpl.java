package com.finalproject.ecommerce.ecommerce.iam.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.iam.application.internal.outboundservices.hashing.HashingService;
import com.finalproject.ecommerce.ecommerce.iam.application.internal.outboundservices.tokens.TokenService;
import com.finalproject.ecommerce.ecommerce.iam.domain.exceptions.AccountNotActivatedException;
import com.finalproject.ecommerce.ecommerce.iam.domain.exceptions.InvalidActivationTokenException;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.RefreshToken;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ActivateAccountCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.DeleteUserCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ResendActivationTokenCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignInCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignUpCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.UpdateUserCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.validators.RavenEmailValidator;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.RefreshTokenCommandService;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserCommandService;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.RefreshTokenRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.finalproject.ecommerce.ecommerce.notifications.domain.model.commands.SendEmailCommand;
import com.finalproject.ecommerce.ecommerce.notifications.domain.model.valueobjects.EmailTemplate;
import com.finalproject.ecommerce.ecommerce.notifications.domain.services.EmailCommandService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
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
    private final RefreshTokenRepository userTokenRepository;
    private final EmailCommandService emailCommandService;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            HashingService hashingService,
            TokenService tokenService,
            RoleRepository roleRepository,
            RefreshTokenCommandService refreshTokenCommandService,
            RefreshTokenRepository userTokenRepository,
            EmailCommandService emailCommandService) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
        this.refreshTokenCommandService = refreshTokenCommandService;
        this.userTokenRepository = userTokenRepository;
        this.emailCommandService = emailCommandService;
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
        Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);

        var activationToken = new RefreshToken(hashedToken, user, expiresAt);
        userTokenRepository.save(activationToken);

        sendActivationEmail(user.getEmail(), user.getUsername(), rawToken);

        return userRepository.findByUsername(command.username());
    }

    private void sendActivationEmail(String email, String username, String rawActivationToken) {
        try {
            Map<String, Object> templateData = Map.of(
                    "username", username,
                    "activationUrl", "http://localhost:8080/api/v1/auth/activate?token=" + rawActivationToken
            );

            var emailCommand = new SendEmailCommand(
                    email,
                    EmailTemplate.WELCOME,
                    templateData,
                    "Activate Your Account"
            );

            emailCommandService.handle(emailCommand);
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

            var potentialTokens = userTokenRepository.findAll().stream()
                    .filter(token -> !token.isUsed() && !token.isExpired())
                    .filter(token -> hashingService.matches(rawToken, token.getTokenHash()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidActivationTokenException("Token not found or invalid"));

            var user = userRepository.findById(potentialTokens.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.activate();
            userRepository.save(user);

            potentialTokens.markAsUsed();
            userTokenRepository.save(potentialTokens);

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

        var existingTokens = userTokenRepository.findAll().stream()
                .filter(token -> token.belongsToUser(user) && !token.isUsed() && !token.isExpired())
                .toList();

        existingTokens.forEach(token -> {
            token.markAsUsed();
            userTokenRepository.save(token);
            log.info("Previous activation token invalidated for user: {}", user.getUsername());
        });

        String rawToken = UUID.randomUUID().toString();

        String hashedToken = hashingService.encode(rawToken);

        Instant expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);

        var activationToken = new RefreshToken(hashedToken, user, expiresAt);
        userTokenRepository.save(activationToken);

        log.info("New activation token generated for user: {}", user.getUsername());

        sendActivationEmail(user.getEmail(), user.getUsername(), rawToken);

        return true;
    }
}