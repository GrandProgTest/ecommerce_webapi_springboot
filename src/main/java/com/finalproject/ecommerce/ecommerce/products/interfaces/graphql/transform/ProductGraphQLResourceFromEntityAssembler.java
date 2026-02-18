package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.ProductGraphQLResource;

public class ProductGraphQLResourceFromEntityAssembler {

    public static ProductGraphQLResource toResourceFromEntity(Product product) {
        return new ProductGraphQLResource(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice().doubleValue(),
                product.getStock(),
                product.getIsActive(),
                product.getCategoryIds(),
                product.getCreatedByUserId(),
                product.getLikesCount(),
                null,
                product.getStock() > 0,
                product.getPrimaryImageUrl(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public static ProductGraphQLResource toResourceFromEntity(Product product, Long userId) {
        boolean isLiked = product.isLikedByUser(userId);

        return new ProductGraphQLResource(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice().doubleValue(),
                product.getStock(),
                product.getIsActive(),
                product.getCategoryIds(),
                product.getCreatedByUserId(),
                product.getLikesCount(),
                isLiked,
                product.getStock() > 0,
                product.getPrimaryImageUrl(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}

