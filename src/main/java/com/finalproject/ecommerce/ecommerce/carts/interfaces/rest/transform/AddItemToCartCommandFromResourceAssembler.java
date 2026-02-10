package com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.AddProductToCartCommand;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.resources.AddItemToCartResource;

public class AddItemToCartCommandFromResourceAssembler {

    public static AddProductToCartCommand toCommandFromResource(Long userId, AddItemToCartResource resource) {
        return new AddProductToCartCommand(userId, resource.productId(), resource.quantity());
    }
}

