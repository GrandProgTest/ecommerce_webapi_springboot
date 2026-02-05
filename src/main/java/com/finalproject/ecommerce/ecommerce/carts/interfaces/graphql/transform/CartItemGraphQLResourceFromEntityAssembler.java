package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.entities.CartItem;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources.CartItemGraphQLResource;

import java.time.ZoneId;

public class CartItemGraphQLResourceFromEntityAssembler {

    public static CartItemGraphQLResource toResourceFromEntity(CartItem item) {
        return new CartItemGraphQLResource(item.getId(), item.getProductId(), item.getQuantity(), item.getCreatedAt() != null ? item.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toString() : null, item.getUpdatedAt() != null ? item.getUpdatedAt().toInstant().atZone(ZoneId.systemDefault()).toString() : null);
    }
}
