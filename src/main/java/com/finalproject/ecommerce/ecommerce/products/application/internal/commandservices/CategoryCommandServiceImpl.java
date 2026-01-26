package com.finalproject.ecommerce.ecommerce.products.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateCategoryCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UpdateCategoryCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.Category;
import com.finalproject.ecommerce.ecommerce.products.domain.services.CategoryCommandService;
import com.finalproject.ecommerce.ecommerce.products.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class CategoryCommandServiceImpl implements CategoryCommandService {

    private final CategoryRepository categoryRepository;


    public CategoryCommandServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Long handle(CreateCategoryCommand command) {
        if (categoryRepository.existsByName(command.name()))
            throw new IllegalArgumentException("Category with name %s already exists".formatted(command.name()));
        var category = new Category(command.name());
        try {
            categoryRepository.save(category);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error saving category: %s".formatted(e.getMessage()));
        }
        return category.getId();
    }

    @Override
    public Optional<Category> handle(UpdateCategoryCommand command) {
        if (categoryRepository.existsByName(command.name()))
            throw new IllegalArgumentException("Category with name %s already exists".formatted(command.name()));
        var result = categoryRepository.findById(command.categoryId());
        if (result.isEmpty())
            throw new IllegalArgumentException("Category with id %s not found".formatted(command.categoryId()));
        var categoryToUpdate = result.get();
        try {
            categoryToUpdate.updateName(command.name());
            var updatedCategory = categoryRepository.save(categoryToUpdate);
            return Optional.of(updatedCategory);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while updating category: %s".formatted(e.getMessage()));
        }
    }
}
