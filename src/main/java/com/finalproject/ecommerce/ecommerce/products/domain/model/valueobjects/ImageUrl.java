package com.finalproject.ecommerce.ecommerce.products.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record ImageUrl(String url) {
    public ImageUrl {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Image URL cannot be empty");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("Image URL must be a valid HTTP(S) URL");
        }
    }
}
