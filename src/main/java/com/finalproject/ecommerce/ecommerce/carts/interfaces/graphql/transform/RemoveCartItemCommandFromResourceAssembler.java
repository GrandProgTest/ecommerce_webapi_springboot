package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.RemoveCartItemCommand;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources.RemoveItemFromCartGraphQLInput;

public class RemoveCartItemCommandFromResourceAssembler {

    public static RemoveCartItemCommand toCommandFromResource(RemoveItemFromCartGraphQLInput input) {
        return new RemoveCartItemCommand(input.userId(), input.cartItemId());
    }
}

