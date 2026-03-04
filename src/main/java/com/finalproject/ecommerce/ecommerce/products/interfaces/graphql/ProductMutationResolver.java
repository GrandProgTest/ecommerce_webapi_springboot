package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.CategoryNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductByIdQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.services.CategoryCommandService;
import com.finalproject.ecommerce.ecommerce.products.domain.services.CategoryQueryService;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductCommandService;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductQueryService;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.mapper.ProductGraphQLMapper;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.mapper.ProductGraphQLMapper.*;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class ProductMutationResolver {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;
    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;

    public ProductMutationResolver(ProductCommandService productCommandService, ProductQueryService productQueryService, CategoryCommandService categoryCommandService, CategoryQueryService categoryQueryService) {
        this.productCommandService = productCommandService;
        this.productQueryService = productQueryService;
        this.categoryCommandService = categoryCommandService;
        this.categoryQueryService = categoryQueryService;
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ProductGraphQLResource createProduct(@Argument CreateProductGraphQLInput input) {
        var command = ProductGraphQLMapper.toCreateProductCommand(input);
        var productId = productCommandService.handle(command);
        var product = productQueryService.handle(new GetProductByIdQuery(productId))
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductGraphQLMapper.toResource(product);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ProductGraphQLResource updateProduct(@Argument Long productId, @Argument UpdateProductGraphQLInput input) {
        var command = ProductGraphQLMapper.toUpdateProductCommand(productId, input);
        var updatedProduct = productCommandService.handle(command)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductGraphQLMapper.toResource(updatedProduct);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public CategoryGraphQLResource createCategory(@Argument CreateCategoryGraphQLInput input) {
        var command = ProductGraphQLMapper.toCreateCategoryCommand(input);
        Long categoryId = categoryCommandService.handle(command);
        var category = categoryQueryService.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        return ProductGraphQLMapper.toResource(category);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ProductGraphQLResource assignCategoryToProduct(@Argument Long productId, @Argument Long categoryId) {
        var command = ProductGraphQLMapper.toAssignCategoryCommand(productId, categoryId);
        var updatedProduct = productCommandService.handle(command)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductGraphQLMapper.toResource(updatedProduct);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public DeleteProductGraphQLResponse deleteProduct(@Argument Long id) {
        var command = ProductGraphQLMapper.toDeleteCommand(id);
        productCommandService.handle(command);
        return new DeleteProductGraphQLResponse(true, "Product deleted successfully");
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ProductGraphQLResource softDeleteProduct(@Argument Long id) {
        var command = ProductGraphQLMapper.toSoftDeleteCommand(id);
        var product = productCommandService.handle(command)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductGraphQLMapper.toResource(product);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ProductGraphQLResource activateProduct(@Argument Long id) {
        var command = ProductGraphQLMapper.toActivateCommand(id);
        var product = productCommandService.handle(command)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductGraphQLMapper.toResource(product);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ProductGraphQLResource deactivateProduct(@Argument Long id) {
        var command = ProductGraphQLMapper.toDeactivateCommand(id);
        var product = productCommandService.handle(command)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductGraphQLMapper.toResource(product);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public LikeProductGraphQLResponse toggleProductLike(@Argument Long productId, @Argument Long userId) {
        var command = ProductGraphQLMapper.toLikeCommand(userId, productId);
        boolean isLiked = productCommandService.handle(command);
        var product = productQueryService.handle(new GetProductByIdQuery(productId))
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductGraphQLMapper.toLikeResponse(product, isLiked);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ProductGraphQLResource setProductSalePrice(@Argument Long productId, @Argument SetProductSalePriceGraphQLInput input) {
        var command = ProductGraphQLMapper.toSetSalePriceCommand(productId, input);
        var product = productCommandService.handle(command)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductGraphQLMapper.toResource(product);
    }
}
