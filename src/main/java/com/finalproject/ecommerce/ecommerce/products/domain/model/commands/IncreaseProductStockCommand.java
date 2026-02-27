package com.finalproject.ecommerce.ecommerce.products.domain.model.commands;

public record IncreaseProductStockCommand(Long productId, int quantity) {
}

