package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.AddProductToCartCommand;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources.AddItemToCartGraphQLResource;

public class AddItemToCartCommandFromResourceAssembler {

    public static AddProductToCartCommand toCommandFromResource(AddItemToCartGraphQLResource resource) {
        return new AddProductToCartCommand(resource.userId(), resource.productId(), resource.quantity());
    }
}
