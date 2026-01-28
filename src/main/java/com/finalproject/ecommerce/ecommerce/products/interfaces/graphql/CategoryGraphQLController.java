package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateCategoryCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UpdateCategoryCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetAllCategoriesQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.services.CategoryCommandService;
import com.finalproject.ecommerce.ecommerce.products.domain.services.CategoryQueryService;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.CategoryResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.CreateCategoryInput;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.UpdateCategoryInput;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform.CategoryResourceFromEntityAssembler;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CategoryGraphQLController {

    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;

    public CategoryGraphQLController(CategoryCommandService categoryCommandService, CategoryQueryService categoryQueryService) {
        this.categoryCommandService = categoryCommandService;
        this.categoryQueryService = categoryQueryService;
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public CategoryResource createCategory(@Argument CreateCategoryInput input) {
        var createCategoryCommand = new CreateCategoryCommand(input.name());
        var categoryId = categoryCommandService.handle(createCategoryCommand);
        if (categoryId == null || categoryId == 0L) {
            throw new RuntimeException("Failed to create category");
        }
        var category = categoryQueryService.findById(categoryId);
        if (category.isEmpty()) {
            throw new RuntimeException("Category not found after creation");
        }
        return CategoryResourceFromEntityAssembler.toResourceFromEntity(category.get());
    }

    @QueryMapping
    public CategoryResource getCategoryById(@Argument Long id) {
        var category = categoryQueryService.findById(id);
        if (category.isEmpty()) {
            throw new RuntimeException("Category not found");
        }
        return CategoryResourceFromEntityAssembler.toResourceFromEntity(category.get());
    }

    @QueryMapping
    public List<CategoryResource> getAllCategories() {
        var categories = categoryQueryService.handle(new GetAllCategoriesQuery());
        return categories.stream()
                .map(CategoryResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public CategoryResource updateCategory(@Argument Long id, @Argument UpdateCategoryInput input) {
        var updateCategoryCommand = new UpdateCategoryCommand(id, input.name());
        var updatedCategory = categoryCommandService.handle(updateCategoryCommand);
        if (updatedCategory.isEmpty()) {
            throw new RuntimeException("Category not found");
        }
        return CategoryResourceFromEntityAssembler.toResourceFromEntity(updatedCategory.get());
    }
}
