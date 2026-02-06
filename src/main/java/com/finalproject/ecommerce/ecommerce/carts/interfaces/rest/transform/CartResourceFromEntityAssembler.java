package com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.resources.CartResource;

import java.util.stream.Collectors;

public class CartResourceFromEntityAssembler {

    public static CartResource toResourceFromEntity(Cart entity) {
        return new CartResource(entity.getId(), entity.getUserId(), entity.getStatus().getName(), entity.getItems().stream().map(CartItemResourceFromEntityAssembler::toResourceFromEntity).collect(Collectors.toList()), entity.getTotalItems(), entity.getCreatedAt(), entity.getCheckedOutAt());
    }
}
