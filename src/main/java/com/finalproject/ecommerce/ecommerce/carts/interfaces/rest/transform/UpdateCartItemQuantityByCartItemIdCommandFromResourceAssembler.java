package com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.UpdateCartItemQuantityByCartItemIdCommand;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.resources.UpdateCartItemQuantityResource;

public class UpdateCartItemQuantityByCartItemIdCommandFromResourceAssembler {

    public static UpdateCartItemQuantityByCartItemIdCommand toCommandFromResource(Long userId, Long cartItemId, UpdateCartItemQuantityResource resource) {
        return new UpdateCartItemQuantityByCartItemIdCommand(userId, cartItemId, resource.quantity());
    }
}

