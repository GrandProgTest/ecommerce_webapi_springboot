package com.finalproject.ecommerce.ecommerce.products.domain.model.commands;
public record DeleteProductImageCommand(
    Long imageId
) {
    public DeleteProductImageCommand {
        if (imageId == null || imageId <= 0) {
            throw new IllegalArgumentException("Image ID must be a positive number");
        }
    }
}
