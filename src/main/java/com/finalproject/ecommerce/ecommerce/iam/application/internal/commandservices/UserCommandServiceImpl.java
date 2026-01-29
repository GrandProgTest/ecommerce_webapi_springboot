package com.finalproject.ecommerce.ecommerce.iam.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.iam.application.internal.outboundservices.hashing.HashingService;
import com.finalproject.ecommerce.ecommerce.iam.application.internal.outboundservices.tokens.TokenService;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.DeleteUserCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignInCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignUpCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.UpdateUserCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.RefreshTokenCommandService;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserCommandService;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;
    private final RefreshTokenCommandService refreshTokenCommandService;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            HashingService hashingService,
            TokenService tokenService,
            RoleRepository roleRepository,
            RefreshTokenCommandService refreshTokenCommandService) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
        this.refreshTokenCommandService = refreshTokenCommandService;
    }


    @Override
    public Optional<ImmutablePair<ImmutablePair<User, String>, String>> handle(SignInCommand command) {
        var user = userRepository.findByUsername(command.username());
        if (user.isEmpty())
            throw new RuntimeException("User not found");
        if (!hashingService.matches(command.password(), user.get().getPassword()))
            throw new RuntimeException("Invalid password");

        var accessToken = tokenService.generateToken(user.get().getUsername());

        var refreshToken = refreshTokenCommandService.createRefreshToken(user.get());

        return Optional.of(
            ImmutablePair.of(
                ImmutablePair.of(user.get(), accessToken),
                refreshToken
            )
        );
    }


    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username()))
            throw new RuntimeException("Username already exists");

        if (userRepository.existsByEmail(command.email()))
            throw new RuntimeException("Email already exists");

        var role = roleRepository.findByName(command.role().getName())
                .orElseThrow(() -> new RuntimeException("Role name not found"));

        var user = new User(
            command.username(),
            command.email(),
            hashingService.encode(command.password()),
            role
        );

        userRepository.save(user);
        return userRepository.findByUsername(command.username());
    }
    @Override
    public Optional<User> handle(UpdateUserCommand command) {
        var user = userRepository.findById(command.userId())
                .orElseThrow(() ->
                        new RuntimeException("User with id %s not found".formatted(command.userId()))
                );

        if (userRepository.existsByUsernameAndIdIsNot(command.username(), command.userId()))
            throw new RuntimeException("User with username %s already exists".formatted(command.username()));

        if (userRepository.existsByEmailAndIdIsNot(command.email(), command.userId()))
            throw new RuntimeException("User with email %s already exists".formatted(command.email()));

        var roleName = command.role().getName();
        var role = roleRepository.findByName(roleName)
                .orElseThrow(() ->
                        new RuntimeException("Role with name %s not found".formatted(roleName))
                );

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
}