package com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.UpdateCartItemQuantityCommand;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.resources.UpdateCartItemQuantityResource;

public class UpdateCartItemQuantityCommandFromResourceAssembler {

    public static UpdateCartItemQuantityCommand toCommandFromResource(Long userId, Long productId, UpdateCartItemQuantityResource resource) {
        return new UpdateCartItemQuantityCommand(
            userId,
            productId,
            resource.quantity()
        );
    }
}
