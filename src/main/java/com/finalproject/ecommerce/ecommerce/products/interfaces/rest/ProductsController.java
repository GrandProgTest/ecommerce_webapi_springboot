package com.finalproject.ecommerce.ecommerce.products.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.AssignCategoryToProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeleteProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetAllProductsQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductByIdQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductCommandService;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductQueryService;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.CreateProductResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.ProductResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.UpdateProductResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.transform.CreateProductCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.transform.ProductResourceFromEntityAssembler;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.transform.UpdateProductCommandFromResourceAssembler;
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
    @Operation(summary = "Get product by id", description = "Get product details by id. Public endpoint - No authentication required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<ProductResource> getProductById(@PathVariable Long productId) {
        var getProductByIdQuery = new GetProductByIdQuery(productId);
        var product = productQueryService.handle(getProductByIdQuery);
        if (product.isEmpty()) return ResponseEntity.notFound().build();
        var productEntity = product.get();
        var productResource = ProductResourceFromEntityAssembler.toResourceFromEntity(productEntity);
        return ResponseEntity.ok(productResource);
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Get all products. Public endpoint - No authentication required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully (may be empty list)")})
    public ResponseEntity<List<ProductResource>> getAllProducts() {
        var products = productQueryService.handle(new GetAllProductsQuery());
        var productResources = products.stream()
                .map(ProductResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(productResources);
    }
}
