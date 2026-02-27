package com.finalproject.ecommerce.ecommerce.products.domain.model.commands;

public record DecreaseProductStockCommand(Long productId, int quantity) {
}
