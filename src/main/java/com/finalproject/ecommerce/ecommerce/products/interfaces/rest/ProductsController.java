package com.finalproject.ecommerce.ecommerce.products.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.ActivateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.AssignCategoryToProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeactivateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeleteProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.ToggleProductLikeCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductByIdQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductsByCategoryWithPaginationQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductsWithPaginationQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductCommandService;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductQueryService;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.*;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.transform.*;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.InvalidPageSizeException;
import com.finalproject.ecommerce.ecommerce.shared.interfaces.rest.resources.PageMetadata;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping(value = "/api/v1/products", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Products", description = "Available Products Endpoints")
public class ProductsController {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;


    public ProductsController(ProductCommandService productCommandService, ProductQueryService productQueryService) {
        this.productCommandService = productCommandService;
        this.productQueryService = productQueryService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Create a new product", description = "Create a new product. Only users with ROLE_MANAGER can create products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_MANAGER"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<ProductResource> createProduct(@RequestBody CreateProductResource resource) {
        var createProductCommand = CreateProductCommandFromResourceAssembler.toCommandFromResource(resource);
        var productId = productCommandService.handle(createProductCommand);

        if (productId == null || productId == 0L) return ResponseEntity.badRequest().build();
        var getProductByIdQuery = new GetProductByIdQuery(productId);
        var product = productQueryService.handle(getProductByIdQuery);

        if (product.isEmpty()) return ResponseEntity.notFound().build();
        var productEntity = product.get();
        var productResource = ProductResourceFromEntityAssembler.toResourceFromEntity(productEntity);
        return new ResponseEntity<>(productResource, HttpStatus.CREATED);
    }


    @PutMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Update product", description = "Update product. Only ROLE_MANAGER can update products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_MANAGER"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<ProductResource> updateProduct(@PathVariable Long productId, @RequestBody UpdateProductResource resource) {
        var updateProductCommand = UpdateProductCommandFromResourceAssembler.toCommandFromResource(productId, resource);
        var updatedProduct = productCommandService.handle(updateProductCommand);
        if (updatedProduct.isEmpty()) return ResponseEntity.notFound().build();
        var updatedProductEntity = updatedProduct.get();
        var updatedProductResource = ProductResourceFromEntityAssembler.toResourceFromEntity(updatedProductEntity);
        return ResponseEntity.ok(updatedProductResource);
    }


    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Delete product", description = "Delete product. Only ROLE_MANAGER can delete products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_MANAGER"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        var deleteProductCommand = new DeleteProductCommand(productId);
        productCommandService.handle(deleteProductCommand);
        return ResponseEntity.ok("Product with given id successfully deleted");
    }

    @PatchMapping("/{productId}/soft-delete")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Soft delete product", description = "Soft delete a product by marking it as deleted. Only ROLE_MANAGER can soft delete products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product soft deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_MANAGER"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<ProductResource> softDeleteProduct(@PathVariable Long productId) {
        var softDeleteCommand = SoftDeleteProductCommandFromResourceAssembler.toCommandFromResource(productId);
        var softDeletedProduct = productCommandService.handle(softDeleteCommand);
        if (softDeletedProduct.isEmpty()) return ResponseEntity.notFound().build();
        var softDeletedProductEntity = softDeletedProduct.get();
        var softDeletedProductResource = ProductResourceFromEntityAssembler.toResourceFromEntity(softDeletedProductEntity);
        return ResponseEntity.ok(softDeletedProductResource);
    }

    @PostMapping("/{productId}/category")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Assign category to product", description = "Assign an existing category to a product. Only ROLE_MANAGER can assign categories.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category assigned to product"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_MANAGER"),
            @ApiResponse(responseCode = "404", description = "Product or category not found"),
            @ApiResponse(responseCode = "400", description = "Category already assigned to product")})
    public ResponseEntity<ProductResource> assignCategoryToProduct(
            @PathVariable Long productId,
            @RequestParam Long categoryId) {
        var assignCategoryCommand = new AssignCategoryToProductCommand(productId, categoryId);
        var updatedProduct = productCommandService.handle(assignCategoryCommand);
        if (updatedProduct.isEmpty()) return ResponseEntity.notFound().build();
        var updatedProductEntity = updatedProduct.get();
        var updatedProductResource = ProductResourceFromEntityAssembler.toResourceFromEntity(updatedProductEntity);
        return ResponseEntity.ok(updatedProductResource);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by id", description = "Get product details by id including all images. Public endpoint - No authentication required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<ProductDetailResource> getProductById(@PathVariable Long productId) {
        var getProductByIdQuery = new GetProductByIdQuery(productId);
        var product = productQueryService.handle(getProductByIdQuery);
        if (product.isEmpty()) return ResponseEntity.notFound().build();
        var productEntity = product.get();
        var productDetailResource = ProductDetailResourceFromEntityAssembler.toResourceFromEntity(productEntity);
        return ResponseEntity.ok(productDetailResource);
    }

    @PostMapping("/users/{userId}/products/{productId}/like")
    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    @Operation(summary = "Toggle like on a product",
            description = "Like or unlike a product. If the user hasn't liked the product, it will add a like. If the user has already liked it, it will remove the like. Only users with ROLE_CLIENT can like products. Users can only like products for themselves.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Like toggled successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_CLIENT or user can only like for themselves"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<ToggleProductLikeResource> toggleProductLike(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        var toggleCommand = new ToggleProductLikeCommand(userId, productId);
        boolean isLiked = productCommandService.handle(toggleCommand);

        var product = productQueryService.handle(new GetProductByIdQuery(productId))
                .orElseThrow(() -> new IllegalStateException("Product not found after toggle"));

        String message = isLiked ? "Product liked successfully" : "Product like removed successfully";
        var response = new ToggleProductLikeResource(
                productId,
                isLiked,
                product.getLikesCount(),
                message
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Toggle product active status",
            description = "Toggle a product's active status. If active, it will be deactivated. If inactive, it will be activated. Only ROLE_MANAGER can toggle product status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product status toggled successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot activate product (e.g., product has no stock)"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_MANAGER"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<ProductResource> toggleProductActiveStatus(@PathVariable Long productId) {
        var product = productQueryService.handle(new GetProductByIdQuery(productId));
        if (product.isEmpty()) return ResponseEntity.notFound().build();

        var currentProduct = product.get();
        var updatedProduct = currentProduct.isActive()
                ? productCommandService.handle(new DeactivateProductCommand(productId))
                : productCommandService.handle(new ActivateProductCommand(productId));

        if (updatedProduct.isEmpty()) return ResponseEntity.notFound().build();
        var updatedProductEntity = updatedProduct.get();
        var updatedProductResource = ProductResourceFromEntityAssembler.toResourceFromEntity(updatedProductEntity);
        return ResponseEntity.ok(updatedProductResource);
    }

    @GetMapping
    @Operation(summary = "Get all products with pagination and filtering",
            description = "Get paginated list of products with sorting options and optional category filter. Public endpoint - No authentication required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully with pagination metadata (may be empty list if no products or category has no products)"),
            @ApiResponse(responseCode = "400", description = "Invalid page size - Allowed values are: 20, 50, 100")})
    public ResponseEntity<PaginatedProductResponse> getAllProducts(
            @Parameter(description = "Category ID for filtering (optional)", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of products per page (allowed: 20, 50, 100)", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Field to sort by (name, price, createdAt)", example = "name")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDirection) {

        if (size != 20 && size != 50 && size != 100) {
            throw new InvalidPageSizeException(size);
        }

        var productPage = categoryId != null
                ? productQueryService.handle(new GetProductsByCategoryWithPaginationQuery(categoryId, page, size, sortBy, sortDirection))
                : productQueryService.handle(new GetProductsWithPaginationQuery(page, size, sortBy, sortDirection));

        var productResources = productPage.getContent().stream()
                .map(ProductResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        var pageMetadata = new PageMetadata(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.hasNext(),
                productPage.hasPrevious()
        );

        var response = new PaginatedProductResponse(productResources, pageMetadata);
        return ResponseEntity.ok(response);
    }
}
