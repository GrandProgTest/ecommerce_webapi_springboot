package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources;

public record ToggleProductLikeResource(Long productId, boolean isLiked, int totalLikes, String message) {
}
