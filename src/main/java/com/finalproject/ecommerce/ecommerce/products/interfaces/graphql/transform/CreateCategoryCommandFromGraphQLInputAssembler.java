package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateCategoryCommand;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.CreateCategoryGraphQLInput;

public class CreateCategoryCommandFromGraphQLInputAssembler {

    public static CreateCategoryCommand toCommandFromInput(CreateCategoryGraphQLInput input) {
        return new CreateCategoryCommand(input.name());
    }
}
