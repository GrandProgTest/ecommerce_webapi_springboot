package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.InvalidOperationException;

public class ProductNotLikedException extends InvalidOperationException {
    public ProductNotLikedException(Long userId, Long productId) {
        super(String.format("User %d has not liked product %d", userId, productId));
    }
}
