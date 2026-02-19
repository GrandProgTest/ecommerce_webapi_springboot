package com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.RemoveCartItemCommand;

public class RemoveCartItemCommandFromResourceAssembler {

    public static RemoveCartItemCommand toCommandFromResource(Long userId, Long cartItemId) {
        return new RemoveCartItemCommand(userId, cartItemId);
    }
}
