package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.UpdateUserCommand;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.UpdateUserResource;

public class UpdateUserCommandFromResourceAssembler {
    public static UpdateUserCommand toCommandFromResource(Long userId, UpdateUserResource resource) {
        return new UpdateUserCommand(userId, resource.username(), resource.password());
    }
}
