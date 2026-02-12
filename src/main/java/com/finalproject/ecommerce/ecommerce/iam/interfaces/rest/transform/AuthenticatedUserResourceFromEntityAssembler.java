package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {

    public static AuthenticatedUserResource toResourceFromEntity(User user, String accessToken, String refreshToken) {
        return new AuthenticatedUserResource(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            accessToken,
            refreshToken
        );
    }
}
