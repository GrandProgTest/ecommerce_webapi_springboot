package com.finalproject.ecommerce.ecommerce.iam.domain.services;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.DeleteUserCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignInCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignUpCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.UpdateUserCommand;

import java.util.Optional;

public interface UserCommandService {
    Long handle(SignUpCommand command);
    Optional<User> handle(SignInCommand command);
    Optional<User> handle(UpdateUserCommand command);
    void handle(DeleteUserCommand command);
}
