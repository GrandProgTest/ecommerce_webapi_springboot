package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UpdateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.UpdateProductResource;

public class UpdateProductCommandFromResourceAssembler {

    public static UpdateProductCommand toCommandFromResource(Long productId, UpdateProductResource resource) {
        return new UpdateProductCommand(
            productId,
            resource.name(),
            resource.description(),
            resource.price(),
            resource.stock()
        );
    }
}
