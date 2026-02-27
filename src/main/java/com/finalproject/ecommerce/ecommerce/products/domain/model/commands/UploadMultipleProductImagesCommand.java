package com.finalproject.ecommerce.ecommerce.products.domain.model.commands;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record UploadMultipleProductImagesCommand(Long productId, List<MultipartFile> imageFiles,
                                                 Integer primaryImageIndex) {
    public UploadMultipleProductImagesCommand {
        if (imageFiles == null || imageFiles.isEmpty()) {
            throw new IllegalArgumentException("Image files list cannot be null or empty");
        }
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be positive");
        }
        if (primaryImageIndex != null && (primaryImageIndex < 0 || primaryImageIndex >= imageFiles.size())) {
            throw new IllegalArgumentException("Primary image index must be between 0 and " + (imageFiles.size() - 1));
        }
    }
}

