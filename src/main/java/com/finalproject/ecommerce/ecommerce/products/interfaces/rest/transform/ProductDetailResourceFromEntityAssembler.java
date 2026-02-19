package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.ProductDetailResource;

public class ProductDetailResourceFromEntityAssembler {

    public static ProductDetailResource toResourceFromEntity(Product entity) {
        var images = entity.getImages().stream()
                .map(ProductImageResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return new ProductDetailResource(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getPrice(),
            entity.getSalePrice(),
            entity.getSalePriceExpireDate(),
            entity.getEffectivePrice(),
            entity.hasActiveSalePrice(),
            entity.getStock(),
            entity.getIsActive(),
            entity.getIsDeleted(),
            entity.getCategoryIds(),
            entity.getCreatedByUserId(),
            images
        );
    }
}

