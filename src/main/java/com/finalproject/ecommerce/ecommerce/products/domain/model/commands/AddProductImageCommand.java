package com.finalproject.ecommerce.ecommerce.products.domain.model.commands;

public record AddProductImageCommand(Long productId, String imageUrl, Boolean isPrimary) {
}
