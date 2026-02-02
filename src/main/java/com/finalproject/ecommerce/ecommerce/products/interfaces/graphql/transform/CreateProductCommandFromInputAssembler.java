package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.CreateProductInput;

public class CreateProductCommandFromInputAssembler {

    public static CreateProductCommand toCommandFromInput(CreateProductInput input) {
        return new CreateProductCommand(
            input.name(),
            input.description(),
            input.price(),
            input.stock(),
            input.categoryIds()
        );
    }
}
