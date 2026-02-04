package com.finalproject.ecommerce.ecommerce.products.domain.model.commands;

public record UnlikeProductCommand(Long userId, Long productId) {
}
