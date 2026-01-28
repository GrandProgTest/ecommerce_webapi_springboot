package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignInCommand;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandFromResource(SignInResource resource) {
        return new SignInCommand(resource.username(), resource.password());
    }
}
