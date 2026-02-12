package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.RefreshTokenCommand;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.RefreshTokenResource;

public class RefreshTokenCommandFromResourceAssembler {

    public static RefreshTokenCommand toCommandFromResource(RefreshTokenResource resource) {
        return new RefreshTokenCommand(resource.refreshToken());
    }
}
