package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.UpdateAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.UpdateAddressResource;

public class UpdateAddressCommandFromResourceAssembler {
    public static UpdateAddressCommand toCommandFromResource(Long addressId, UpdateAddressResource resource) {
        return new UpdateAddressCommand(
                addressId,
                resource.street(),
                resource.city(),
                resource.state(),
                resource.country(),
                resource.postalCode()
        );
    }
}
