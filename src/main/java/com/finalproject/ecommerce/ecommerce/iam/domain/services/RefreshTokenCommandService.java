package com.finalproject.ecommerce.ecommerce.iam.domain.services;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.RefreshTokenCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.RevokeAllUserRefreshTokensCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.RevokeRefreshTokenCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignOutCommand;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Optional;


public interface RefreshTokenCommandService {


    String createRefreshToken(User user);
    Optional<ImmutablePair<ImmutablePair<User, String>, String>> handle(RefreshTokenCommand command);
    void handle(RevokeRefreshTokenCommand command);
    void handle(RevokeAllUserRefreshTokensCommand command);
    void handle(SignOutCommand command);
}
