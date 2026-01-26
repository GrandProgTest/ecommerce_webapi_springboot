package com.finalproject.ecommerce.ecommerce.products.domain.model.commands;

public record DeleteProductCommand (Long productId) {
    public DeleteProductCommand{
        if(productId == null || productId <= 0){
            throw new IllegalArgumentException("productId cannot be null or less than 1");
        }
    }
}
