package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignUpCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.SignUpResource;

public class SignUpCommandFromResourceAssembler {

    public static SignUpCommand toCommandFromResource(SignUpResource resource) {

        Role role = null;

        if (resource.role() != null) {
            role = Role.toRoleFromName(resource.role());
        }

        return new SignUpCommand(
                resource.username(),
                resource.email(),
                resource.password(),
                role
        );
    }
}
