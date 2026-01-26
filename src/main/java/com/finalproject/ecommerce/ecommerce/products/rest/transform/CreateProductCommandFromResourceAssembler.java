package com.finalproject.ecommerce.ecommerce.products.rest.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.rest.resources.CreateProductResource;

public class CreateProductCommandFromResourceAssembler {

    public static CreateProductCommand toCommandFromResource(CreateProductResource resource) {
        return new CreateProductCommand(
            resource.name(),
            resource.description(),
            resource.price(),
            resource.stock(),
            resource.categoryIds(),
            resource.createdByUserId()
        );
    }
}
