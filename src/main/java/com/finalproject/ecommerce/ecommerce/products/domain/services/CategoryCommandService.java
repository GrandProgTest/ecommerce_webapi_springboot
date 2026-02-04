package com.finalproject.ecommerce.ecommerce.products.domain.services;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateCategoryCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UpdateCategoryCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.Category;

import java.util.Optional;

public interface CategoryCommandService {
    Long handle(CreateCategoryCommand command);

    Optional<Category> handle(UpdateCategoryCommand command);
}
