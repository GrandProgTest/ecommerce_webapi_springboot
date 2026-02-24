package com.finalproject.ecommerce.ecommerce.products.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.ActivateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.AssignCategoryToProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeactivateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeleteProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductByIdQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductsWithPaginationQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductCommandService;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductQueryService;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.mapper.ProductRestMapper;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.mapper.ProductRestMapper.*;
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
        var createProductCommand = ProductRestMapper.toCreateCommand(resource);
        var productId = productCommandService.handle(createProductCommand);
        if (productId == null || productId == 0L) return ResponseEntity.badRequest().build();
        var product = productQueryService.handle(new GetProductByIdQuery(productId));
        if (product.isEmpty()) return ResponseEntity.notFound().build();
        return new ResponseEntity<>(ProductRestMapper.toResource(product.get()), HttpStatus.CREATED);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Update product", description = "Update product. Only ROLE_MANAGER can update products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_MANAGER"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<ProductResource> updateProduct(@PathVariable Long productId, @RequestBody UpdateProductResource resource) {
        var command = ProductRestMapper.toUpdateCommand(productId, resource);
        var updatedProduct = productCommandService.handle(command);
        if (updatedProduct.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ProductRestMapper.toResource(updatedProduct.get()));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Delete product", description = "Delete product. Only ROLE_MANAGER can delete products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_MANAGER"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        productCommandService.handle(new DeleteProductCommand(productId));
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
        var command = ProductRestMapper.toSoftDeleteCommand(productId);
        var result = productCommandService.handle(command);
        if (result.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ProductRestMapper.toResource(result.get()));
    }

    @PostMapping("/{productId}/category")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Assign category to product", description = "Assign an existing category to a product. Only ROLE_MANAGER can assign categories.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category assigned to product"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_MANAGER"),
            @ApiResponse(responseCode = "404", description = "Product or category not found"),
            @ApiResponse(responseCode = "400", description = "Category already assigned to product")})
    public ResponseEntity<ProductResource> assignCategoryToProduct(@PathVariable Long productId, @RequestParam Long categoryId) {
        var command = new AssignCategoryToProductCommand(productId, categoryId);
        var updatedProduct = productCommandService.handle(command);
        if (updatedProduct.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ProductRestMapper.toResource(updatedProduct.get()));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by id", description = "Get product details by id including all images. Public endpoint - No authentication required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<ProductDetailResource> getProductById(@PathVariable Long productId) {
        var product = productQueryService.handle(new GetProductByIdQuery(productId));
        if (product.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ProductRestMapper.toDetailResource(product.get()));
    }

    @PostMapping("/users/{userId}/products/{productId}/like")
    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    @Operation(summary = "Toggle like on a product", description = "Like or unlike a product. Only users with ROLE_CLIENT can like products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Like toggled successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_CLIENT"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<ToggleProductLikeResource> toggleProductLike(@PathVariable Long userId, @PathVariable Long productId) {
        var command = ProductRestMapper.toLikeCommand(userId, productId);
        boolean isLiked = productCommandService.handle(command);
        var product = productQueryService.handle(new GetProductByIdQuery(productId))
                .orElseThrow(() -> new ProductNotFoundException(productId));
        String message = isLiked ? "Product liked successfully" : "Product like removed successfully";
        return ResponseEntity.ok(new ToggleProductLikeResource(productId, isLiked, product.getLikesCount(), message));
    }

    @PatchMapping("/{productId}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Toggle product active status", description = "Toggle a product's active status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product status toggled successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot activate product"),
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
        return ResponseEntity.ok(ProductRestMapper.toResource(updatedProduct.get()));
    }

    @GetMapping
    @Operation(summary = "Get all products with pagination and filtering", description = "Get paginated list of products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid page size - Allowed values are: 20, 50, 100")})
    public ResponseEntity<PaginatedProductResponse> getAllProducts(
            @Parameter(description = "Category ID for filtering (optional)") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of products per page (allowed: 20, 50, 100)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "asc") String sortDirection) {

        if (size != 20 && size != 50 && size != 100) throw new InvalidPageSizeException(size);

        var productPageResponse = productQueryService.handle(
                new GetProductsWithPaginationQuery(categoryId, null, page, size, sortBy, sortDirection)
        );

        var productResources = productPageResponse.products().stream()
                .map(p -> new ProductResource(p.id(), p.name(), p.description(), p.price(), p.salePrice(), p.salePriceExpireDate(), p.effectivePrice(), p.hasActiveSalePrice(), p.stock(), p.isActive(), p.isDeleted(), p.categoryIds(), p.createdByUserId(), p.primaryImageUrl()))
                .toList();

        var meta = productPageResponse.pageMetadata();
        var pageMetadata = new PageMetadata(meta.currentPage(), meta.pageSize(), meta.totalElements(), meta.totalPages(), meta.hasNext(), meta.hasPrevious());

        return ResponseEntity.ok(new PaginatedProductResponse(productResources, pageMetadata));
    }

    @PatchMapping("/{productId}/sale-price")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    @Operation(summary = "Set product sale price", description = "Set a temporary sale price for a product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sale price set successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_MANAGER"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<ProductResource> setProductSalePrice(@PathVariable Long productId, @RequestBody SetProductSalePriceResource resource) {
        var command = ProductRestMapper.toSetSalePriceCommand(productId, resource);
        var updatedProduct = productCommandService.handle(command);
        if (updatedProduct.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ProductRestMapper.toResource(updatedProduct.get()));
    }
}
