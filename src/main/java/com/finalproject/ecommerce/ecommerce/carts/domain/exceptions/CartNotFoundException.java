package com.finalproject.ecommerce.ecommerce.carts.domain.exceptions;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(Long cartId) {
        super("Cart with id " + cartId + " not found");
    }

    public CartNotFoundException(String message) {
        super(message);
    }
}
