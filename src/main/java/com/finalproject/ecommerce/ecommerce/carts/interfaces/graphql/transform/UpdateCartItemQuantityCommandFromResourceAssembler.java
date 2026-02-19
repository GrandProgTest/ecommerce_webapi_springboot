package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.UpdateCartItemQuantityByCartItemIdCommand;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources.UpdateCartItemQuantityGraphQLResource;

public class UpdateCartItemQuantityCommandFromResourceAssembler {

    public static UpdateCartItemQuantityByCartItemIdCommand toCommandFromResource(UpdateCartItemQuantityGraphQLResource resource) {
        return new UpdateCartItemQuantityByCartItemIdCommand(resource.userId(), resource.cartItemId(), resource.quantity());
    }
}

