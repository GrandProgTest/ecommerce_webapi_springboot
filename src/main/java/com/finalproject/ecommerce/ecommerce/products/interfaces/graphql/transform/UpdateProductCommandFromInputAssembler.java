package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform;


import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UpdateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.UpdateProductInput;

public class UpdateProductCommandFromInputAssembler {

    public static UpdateProductCommand toCommandFromInput(Long productId, UpdateProductInput input) {
        return new UpdateProductCommand(
            productId,
            input.name(),
            input.description(),
            input.price(),
            input.stock()
        );
    }
}
