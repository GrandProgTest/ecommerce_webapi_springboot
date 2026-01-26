package com.finalproject.ecommerce.ecommerce.products.rest.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateCategoryCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UpdateCategoryCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.Category;
import com.finalproject.ecommerce.ecommerce.products.rest.resources.CategoryResource;
import com.finalproject.ecommerce.ecommerce.products.rest.resources.CreateCategoryResource;
import com.finalproject.ecommerce.ecommerce.products.rest.resources.UpdateCategoryResource;

public class CategoryResourceFromEntityAssembler {

    public static CategoryResource toResourceFromEntity(Category entity) {
        return new CategoryResource(
            entity.getId(),
            entity.getName(),
            entity.getCreatedAt()
        );
    }

    public static CreateCategoryCommand toCommandFromResource(CreateCategoryResource resource) {
        return new CreateCategoryCommand(resource.name());
    }

    public static UpdateCategoryCommand toCommandFromResource(Long categoryId, UpdateCategoryResource resource) {
        return new UpdateCategoryCommand(categoryId, resource.name());
    }
}
