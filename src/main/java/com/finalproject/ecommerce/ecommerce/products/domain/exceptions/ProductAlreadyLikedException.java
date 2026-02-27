package com.finalproject.ecommerce.ecommerce.products.domain.exceptions;

import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.BusinessRuleException;

public class ProductAlreadyLikedException extends BusinessRuleException {
    public ProductAlreadyLikedException(Long userId, Long productId) {
        super(String.format("User %d already liked product %d", userId, productId));
    }
}
