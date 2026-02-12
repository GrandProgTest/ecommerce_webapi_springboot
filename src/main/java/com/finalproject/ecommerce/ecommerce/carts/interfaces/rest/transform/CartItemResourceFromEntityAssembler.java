package com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.entities.CartItem;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.resources.CartItemResource;

public class CartItemResourceFromEntityAssembler {

    public static CartItemResource toResourceFromEntity(CartItem entity) {
        return new CartItemResource(entity.getId(), entity.getProductId(), entity.getQuantity());
    }
}
