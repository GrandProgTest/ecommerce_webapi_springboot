package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform;


import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.ProductResource;

public class ProductResourceFromEntityAssembler {

    public static ProductResource toResourceFromEntity(Product entity) {
        return new ProductResource(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getPriceAmount(),
            entity.getStock(),
            entity.getIsActive(),
            entity.getCategoryIds(),
            entity.getCreatedByUserId()
        );
    }
}
