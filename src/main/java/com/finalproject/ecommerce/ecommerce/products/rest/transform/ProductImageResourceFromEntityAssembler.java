package com.finalproject.ecommerce.ecommerce.products.rest.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductImage;
import com.finalproject.ecommerce.ecommerce.products.rest.resources.ProductImageResource;

public class ProductImageResourceFromEntityAssembler {

    public static ProductImageResource toResourceFromEntity(ProductImage entity) {
        return new ProductImageResource(
            entity.getId(),
            entity.getProductId(),
            entity.getUrl(),
            entity.getIsPrimary(),
            entity.getCreatedAt()
        );
    }
}
