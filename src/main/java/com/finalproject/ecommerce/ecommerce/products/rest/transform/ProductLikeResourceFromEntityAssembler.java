package com.finalproject.ecommerce.ecommerce.products.rest.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductLike;
import com.finalproject.ecommerce.ecommerce.products.rest.resources.ProductLikeResource;

public class ProductLikeResourceFromEntityAssembler {

    public static ProductLikeResource toResourceFromEntity(ProductLike entity) {
        return new ProductLikeResource(
            entity.getUserId(),
            entity.getProductId(),
            entity.getLikedAt()
        );
    }
}
