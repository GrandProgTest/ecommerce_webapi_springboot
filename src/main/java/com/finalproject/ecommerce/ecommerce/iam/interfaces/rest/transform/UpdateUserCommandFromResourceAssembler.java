package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.UpdateUserCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.UpdateUserResource;

public class UpdateUserCommandFromResourceAssembler {

    public static UpdateUserCommand toCommandFromResource(Long userId, UpdateUserResource resource) {
        Role role = null;

        if (resource.role() != null) {
            role = Role.toRoleFromName(resource.role());
        }
        return new UpdateUserCommand(userId, resource.username(), resource.email(), resource.password(), role);
    }
}
