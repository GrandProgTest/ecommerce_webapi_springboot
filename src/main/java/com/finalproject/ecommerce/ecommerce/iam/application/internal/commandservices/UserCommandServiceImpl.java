package com.finalproject.ecommerce.ecommerce.iam.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.DeleteUserCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignInCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignUpCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.UpdateUserCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserCommandService;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserCommandServiceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public Long handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username()))
            throw new IllegalArgumentException("User with username %s already exists".formatted(command.username()));

        var role = roleRepository.findByName(command.role())
                .orElseThrow(() -> new IllegalArgumentException("Role %s not found".formatted(command.role())));

        var user = new User(command.username(), command.password(), role);

        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error saving user: %s".formatted(e.getMessage()));
        }
        return user.getId();
    }

    @Override
    public Optional<User> handle(SignInCommand command) {
        var user = userRepository.findByUsername(command.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.getPassword().equals(command.password()))
            throw new IllegalArgumentException("Invalid credentials");

        return Optional.of(user);
    }

    @Override
    public Optional<User> handle(UpdateUserCommand command) {
        var result = userRepository.findById(command.userId());
        if (result.isEmpty())
            throw new IllegalArgumentException("User with id %s not found".formatted(command.userId()));

        if (userRepository.existsByUsernameAndIdIsNot(command.username(), command.userId()))
            throw new IllegalArgumentException("User with username %s already exists".formatted(command.username()));

        var userToUpdate = result.get();
        userToUpdate.setUsername(command.username());
        userToUpdate.setPassword(command.password());

        try {
            var updatedUser = userRepository.save(userToUpdate);
            return Optional.of(updatedUser);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while updating user: %s".formatted(e.getMessage()));
        }
    }

    @Override
    public void handle(DeleteUserCommand command) {
        if (!userRepository.existsById(command.userId()))
            throw new IllegalArgumentException("User with id %s not found".formatted(command.userId()));

        try {
            userRepository.deleteById(command.userId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while deleting user: %s".formatted(e.getMessage()));
        }
    }
}
