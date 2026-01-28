package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignUpCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.SignUpResource;


public class SignUpCommandFromResourceAssembler {

    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        var role = Role.toRoleFromName(resource.roleName());
        return new SignUpCommand(
                resource.username(),
                null,
                resource.password(),
                role.getName()
        );
    }
}
