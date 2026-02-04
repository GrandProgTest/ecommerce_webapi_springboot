package com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.AddProductToCartCommand;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.resources.AddProductToCartResource;

public class AddProductToCartCommandFromResourceAssembler {

    public static AddProductToCartCommand toCommandFromResource(Long userId, AddProductToCartResource resource) {
        return new AddProductToCartCommand(userId, resource.productId(), resource.quantity());
    }
}
