package com.finalproject.ecommerce.ecommerce.products.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateCategoryCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.UpdateCategoryCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetAllCategoriesQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.services.CategoryCommandService;
import com.finalproject.ecommerce.ecommerce.products.domain.services.CategoryQueryService;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.mapper.ProductRestMapper;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.mapper.ProductRestMapper.CategoryResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.mapper.ProductRestMapper.CreateCategoryResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.mapper.ProductRestMapper.UpdateCategoryResource;
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
    @Operation(summary = "Create a new category")
    @ApiResponses(value = {@ApiResponse(responseCode = "201"), @ApiResponse(responseCode = "400"), @ApiResponse(responseCode = "403")})
    public ResponseEntity<CategoryResource> createCategory(@RequestBody CreateCategoryResource resource) {
        var categoryId = categoryCommandService.handle(new CreateCategoryCommand(resource.name()));
        if (categoryId == null || categoryId == 0L) return ResponseEntity.badRequest().build();
        var category = categoryQueryService.findById(categoryId);
        if (category.isEmpty()) return ResponseEntity.notFound().build();
        return new ResponseEntity<>(ProductRestMapper.toResource(category.get()), HttpStatus.CREATED);
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category by id")
    public ResponseEntity<CategoryResource> getCategoryById(@PathVariable Long categoryId) {
        var category = categoryQueryService.findById(categoryId);
        if (category.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ProductRestMapper.toResource(category.get()));
    }

    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryResource>> getAllCategories() {
        var categories = categoryQueryService.handle(new GetAllCategoriesQuery());
        return ResponseEntity.ok(categories.stream().map(ProductRestMapper::toResource).toList());
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Update category")
    public ResponseEntity<CategoryResource> updateCategory(@PathVariable Long categoryId, @RequestBody UpdateCategoryResource resource) {
        var updatedCategory = categoryCommandService.handle(new UpdateCategoryCommand(categoryId, resource.name()));
        if (updatedCategory.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ProductRestMapper.toResource(updatedCategory.get()));
    }
}
