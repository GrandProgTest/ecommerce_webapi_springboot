package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.AddProductImageResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.CreateProductResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.ProductResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.UpdateProductResource;
import com.finalproject.ecommerce.ecommerce.products.rest.resources.*;

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

    public static CreateProductCommand toCommandFromResource(CreateProductResource resource, Long userId) {
        return new CreateProductCommand(
            resource.name(),
            resource.description(),
            resource.price(),
            resource.stock(),
            resource.categoryIds(),
            userId
        );
    }

    public static UpdateProductCommand toCommandFromResource(Long productId, UpdateProductResource resource) {
        return new UpdateProductCommand(
            productId,
            resource.name(),
            resource.description(),
            resource.price(),
            resource.stock()
        );
    }

    public static AddProductImageCommand toCommandFromResource(Long productId, AddProductImageResource resource) {
        return new AddProductImageCommand(
            productId,
            resource.imageUrl(),
            resource.isPrimary()
        );
    }
}
