package com.finalproject.ecommerce.ecommerce.products.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.CategoryNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.DuplicateCategoryException;
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
            throw new DuplicateCategoryException(command.name());
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
        var result = categoryRepository.findById(command.categoryId());
        if (result.isEmpty())
            throw new CategoryNotFoundException(command.categoryId());

        if (categoryRepository.existsByName(command.name()))
            throw new DuplicateCategoryException(command.name());

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
