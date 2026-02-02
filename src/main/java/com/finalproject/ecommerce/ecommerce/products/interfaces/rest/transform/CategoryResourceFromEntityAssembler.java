package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.Category;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.CategoryResource;

public class CategoryResourceFromEntityAssembler {

    public static CategoryResource toResourceFromEntity(Category entity) {
        return new CategoryResource(
            entity.getId(),
            entity.getName(),
            entity.getCreatedAt()
        );
    }
}
