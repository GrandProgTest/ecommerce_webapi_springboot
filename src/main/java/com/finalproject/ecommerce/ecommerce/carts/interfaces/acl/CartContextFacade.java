package com.finalproject.ecommerce.ecommerce.carts.interfaces.acl;

import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.dto.CartDto;

import java.util.Optional;

public interface CartContextFacade {
    Optional<CartDto> getCartById(Long cartId);

    Optional<CartDto> getActiveCartByUserId(Long userId);
}
