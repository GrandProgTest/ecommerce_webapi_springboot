package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.LikeProductGraphQLResponse;

public class LikeResponseFromEntityAssembler {

    public static LikeProductGraphQLResponse toResponseFromEntity(Product product, Long userId, boolean isLiked) {
        String message = isLiked ? "Product liked successfully" : "Product unliked successfully";
        return new LikeProductGraphQLResponse(true, isLiked, product.getLikesCount(), message);
    }
}

