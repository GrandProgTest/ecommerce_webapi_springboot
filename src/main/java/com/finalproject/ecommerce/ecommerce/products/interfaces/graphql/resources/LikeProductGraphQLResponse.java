package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources;

public record LikeProductGraphQLResponse(Boolean success, Boolean isLiked, Integer likeCount, String message) {
}
