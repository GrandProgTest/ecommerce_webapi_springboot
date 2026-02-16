package com.finalproject.ecommerce.ecommerce.iam.domain.services;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ActivateAccountCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.DeleteUserCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ResendActivationTokenCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignInCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignUpCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.UpdateUserCommand;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Optional;

public interface UserCommandService {
    Optional<User> handle(SignUpCommand command);

    Optional<ImmutablePair<ImmutablePair<User, String>, String>> handle(SignInCommand command);

    Optional<User> handle(UpdateUserCommand command);

    void handle(DeleteUserCommand command);

    boolean handle(ActivateAccountCommand command);

    boolean handle(ResendActivationTokenCommand command);
}
