package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources;

public record UploadImageResponse(Long imageId, String imageUrl, Boolean isPrimary, String message) {
    public static UploadImageResponse success(Long imageId, String imageUrl, Boolean isPrimary) {
        return new UploadImageResponse(
                imageId,
                imageUrl,
                isPrimary,
                "Image uploaded successfully"
        );
    }
}

