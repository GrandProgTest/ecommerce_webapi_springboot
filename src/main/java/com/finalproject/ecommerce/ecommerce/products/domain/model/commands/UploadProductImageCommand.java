package com.finalproject.ecommerce.ecommerce.products.domain.model.commands;

import org.springframework.web.multipart.MultipartFile;

public record UploadProductImageCommand(Long productId, MultipartFile imageFile, Boolean isPrimary) {
    public UploadProductImageCommand {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be null or empty");
        }
        if (isPrimary == null) {
            isPrimary = false;
        }
    }
}
