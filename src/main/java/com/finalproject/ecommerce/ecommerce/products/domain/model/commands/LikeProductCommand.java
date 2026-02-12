package com.finalproject.ecommerce.ecommerce.products.domain.model.commands;

public record LikeProductCommand(Long userId, Long productId) {
}
