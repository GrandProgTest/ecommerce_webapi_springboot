package com.finalproject.ecommerce.ecommerce.products.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateCategoryCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UpdateCategoryCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetAllCategoriesQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.services.CategoryCommandService;
import com.finalproject.ecommerce.ecommerce.products.domain.services.CategoryQueryService;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.CategoryResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.CreateCategoryResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.UpdateCategoryResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.transform.CategoryResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/categories", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Categories", description = "Available Categories Endpoints")
public class CategoriesController {

    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;


    public CategoriesController(CategoryCommandService categoryCommandService, CategoryQueryService categoryQueryService) {
        this.categoryCommandService = categoryCommandService;
        this.categoryQueryService = categoryQueryService;
    }


    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Create a new category", description = "Create a new category. Only ROLE_MANAGER can create categories.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_MANAGER"),
            @ApiResponse(responseCode = "404", description = "Category not found")})
    public ResponseEntity<CategoryResource> createCategory(@RequestBody CreateCategoryResource resource) {
        var createCategoryCommand = new CreateCategoryCommand(resource.name());
        var categoryId = categoryCommandService.handle(createCategoryCommand);
        if (categoryId == null || categoryId == 0L) return ResponseEntity.badRequest().build();
        var category = categoryQueryService.findById(categoryId);
        if (category.isEmpty()) return ResponseEntity.notFound().build();
        var categoryEntity = category.get();
        var categoryResource = CategoryResourceFromEntityAssembler.toResourceFromEntity(categoryEntity);
        return new ResponseEntity<>(categoryResource, HttpStatus.CREATED);
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by id", description = "Get category by id. Public endpoint - No authentication required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")})
    public ResponseEntity<CategoryResource> getCategoryById(@PathVariable Long categoryId) {
        var category = categoryQueryService.findById(categoryId);
        if (category.isEmpty()) return ResponseEntity.notFound().build();
        var categoryEntity = category.get();
        var categoryResource = CategoryResourceFromEntityAssembler.toResourceFromEntity(categoryEntity);
        return ResponseEntity.ok(categoryResource);
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Get all categories. Public endpoint - No authentication required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully (may be empty list)")})
    public ResponseEntity<List<CategoryResource>> getAllCategories() {
        var categories = categoryQueryService.handle(new GetAllCategoriesQuery());
        var categoryResources = categories.stream()
                .map(CategoryResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(categoryResources);
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Update category", description = "Update category. Only ROLE_MANAGER can update categories.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_MANAGER"),
            @ApiResponse(responseCode = "404", description = "Category not found")})
    public ResponseEntity<CategoryResource> updateCategory(@PathVariable Long categoryId, @RequestBody UpdateCategoryResource resource) {
        var updateCategoryCommand = new UpdateCategoryCommand(categoryId, resource.name());
        var updatedCategory = categoryCommandService.handle(updateCategoryCommand);
        if (updatedCategory.isEmpty()) return ResponseEntity.notFound().build();
        var updatedCategoryEntity = updatedCategory.get();
        var updatedCategoryResource = CategoryResourceFromEntityAssembler.toResourceFromEntity(updatedCategoryEntity);
        return ResponseEntity.ok(updatedCategoryResource);
    }
}
