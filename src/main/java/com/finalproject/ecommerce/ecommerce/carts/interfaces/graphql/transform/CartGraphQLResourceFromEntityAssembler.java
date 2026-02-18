package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources.CartGraphQLResource;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources.CartItemGraphQLResource;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class CartGraphQLResourceFromEntityAssembler {

    public static CartGraphQLResource toResourceFromEntity(Cart cart) {
        List<CartItemGraphQLResource> items = cart.getItems().stream().map(CartItemGraphQLResourceFromEntityAssembler::toResourceFromEntity).collect(Collectors.toList());

        return new CartGraphQLResource(cart.getId(), cart.getUserId(), cart.getStatus().getName(), items, cart.getTotalItems(), cart.getCreatedAt(), cart.getUpdatedAt(), cart.getCheckedOutAt());
    }

    private static String formatDateTime(Instant instant) {
        return instant != null ? instant.toString() : null;
    }
}
