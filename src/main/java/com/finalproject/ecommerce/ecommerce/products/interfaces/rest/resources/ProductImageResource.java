package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources;

import java.util.Date;

public record ProductImageResource(Long id, Long productId, String imageUrl, Boolean isPrimary,
                                   Date createdAt) {
}
