package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.CreateProductGraphQLInput;

import java.math.BigDecimal;

public class CreateProductCommandFromGraphQLInputAssembler {

    public static CreateProductCommand toCommandFromInput(CreateProductGraphQLInput input) {
        return new CreateProductCommand(
                input.name(),
                input.description(),
                BigDecimal.valueOf(input.price()),
                input.stock(),
                input.categoryIds(),
                true
        );
    }
}

