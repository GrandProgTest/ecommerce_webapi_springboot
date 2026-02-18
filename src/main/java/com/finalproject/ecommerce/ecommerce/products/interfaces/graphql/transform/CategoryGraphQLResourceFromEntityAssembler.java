package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.Category;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.CategoryGraphQLResource;

public class CategoryGraphQLResourceFromEntityAssembler {

    public static CategoryGraphQLResource toResourceFromEntity(Category category) {
        return new CategoryGraphQLResource(
                category.getId(),
                category.getName(),
                category.getCreatedAt()
        );
    }
}

