package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.ProductResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.*;

public class ProductResourceFromEntityAssembler {

    public static ProductResource toResourceFromEntity(Product entity) {
        return new ProductResource(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStock(),
                entity.getIsActive(),
                entity.getIsDeleted(),
                entity.getCategoryIds(),
                entity.getCreatedByUserId(),
                entity.getPrimaryImageUrl()
        );
    }
}
