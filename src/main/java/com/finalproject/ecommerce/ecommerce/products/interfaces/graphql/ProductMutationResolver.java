package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.CategoryNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductByIdQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.services.CategoryCommandService;
import com.finalproject.ecommerce.ecommerce.products.domain.services.CategoryQueryService;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductCommandService;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductQueryService;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.*;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform.*;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ProductMutationResolver {

    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;
    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ProductGraphQLResource createProduct(@Argument CreateProductGraphQLInput input) {
        var command = CreateProductCommandFromGraphQLInputAssembler.toCommandFromInput(input);
        var productId = productCommandService.handle(command);

        var product = productQueryService.handle(new GetProductByIdQuery(productId))
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return ProductGraphQLResourceFromEntityAssembler.toResourceFromEntity(product);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public CategoryGraphQLResource createCategory(@Argument CreateCategoryGraphQLInput input) {
        var command = CreateCategoryCommandFromGraphQLInputAssembler.toCommandFromInput(input);
        Long categoryId = categoryCommandService.handle(command);

        var category = categoryQueryService.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        return CategoryGraphQLResourceFromEntityAssembler.toResourceFromEntity(category);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public DeleteProductGraphQLResponse deleteProduct(@Argument Long id) {
        var command = ProductCommandFromGraphQLResourceAssembler.toDeleteCommandFromId(id);
        productCommandService.handle(command);
        return new DeleteProductGraphQLResponse(true, "Product deleted successfully");
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ProductGraphQLResource activateProduct(@Argument Long id) {
        var command = ProductCommandFromGraphQLResourceAssembler.toActivateCommandFromId(id);
        var product = productCommandService.handle(command)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductGraphQLResourceFromEntityAssembler.toResourceFromEntity(product);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ProductGraphQLResource deactivateProduct(@Argument Long id) {
        var command = ProductCommandFromGraphQLResourceAssembler.toDeactivateCommandFromId(id);
        var product = productCommandService.handle(command)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductGraphQLResourceFromEntityAssembler.toResourceFromEntity(product);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public LikeProductGraphQLResponse likeProduct(@Argument Long productId, @Argument Long userId) {
        var command = LikeCommandFromGraphQLResourceAssembler.toCommandFromIds(userId, productId);
        boolean isLiked = productCommandService.handle(command);

        var product = productQueryService.handle(new GetProductByIdQuery(productId))
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return LikeResponseFromEntityAssembler.toResponseFromEntity(product, userId, isLiked);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public LikeProductGraphQLResponse unlikeProduct(@Argument Long productId, @Argument Long userId) {
        var command = LikeCommandFromGraphQLResourceAssembler.toCommandFromIds(userId, productId);
        boolean isLiked = productCommandService.handle(command);

        var product = productQueryService.handle(new GetProductByIdQuery(productId))
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return LikeResponseFromEntityAssembler.toResponseFromEntity(product, userId, isLiked);
    }

    @MutationMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ProductGraphQLResource setProductSalePrice(@Argument Long productId, @Argument SetProductSalePriceGraphQLInput input) {
        var command = SetProductSalePriceCommandFromGraphQLInputAssembler.toCommandFromInput(productId, input);
        var product = productCommandService.handle(command)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductGraphQLResourceFromEntityAssembler.toResourceFromEntity(product);
    }
}



