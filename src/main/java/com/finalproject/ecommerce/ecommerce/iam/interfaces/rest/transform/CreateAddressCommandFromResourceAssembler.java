package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.CreateAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.CreateAddressResource;

public class CreateAddressCommandFromResourceAssembler {
    public static CreateAddressCommand toCommandFromResource(CreateAddressResource resource) {
        return new CreateAddressCommand(
                resource.street(),
                resource.city(),
                resource.state(),
                resource.country(),
                resource.postalCode(),
                resource.isDefault()
        );
    }
}
