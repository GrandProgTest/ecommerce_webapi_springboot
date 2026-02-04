package com.finalproject.ecommerce.ecommerce.carts.interfaces.acl;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;

import java.util.Optional;

public interface CartContextFacade {
    Optional<Cart> getCartById(Long cartId);

    Optional<Cart> getActiveCartByUserId(Long userId);
}
