package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductByIdQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductsByCategoryWithPaginationQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductsWithPaginationQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductQueryService;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.PageMetadataGraphQLResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.ProductGraphQLResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.ProductPageGraphQLResource;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform.ProductGraphQLResourceFromEntityAssembler;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.InvalidPageSizeException;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ProductQueryResolver {

    private final ProductQueryService productQueryService;

    @QueryMapping
    public ProductGraphQLResource product(@Argument Long id) {
        var query = new GetProductByIdQuery(id);
        var product = productQueryService.handle(query)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductGraphQLResourceFromEntityAssembler.toResourceFromEntity(product);
    }

    @QueryMapping
    public ProductPageGraphQLResource products(
            @Argument Integer page,
            @Argument Integer size,
            @Argument Long categoryId,
            @Argument String sortBy,
            @Argument String sortDirection) {

        if (page == null) page = 0;
        if (size == null) size = 20;
        if (sortBy == null || sortBy.isBlank()) sortBy = "id";
        if (sortDirection == null || sortDirection.isBlank()) sortDirection = "asc";

        if (size != 20 && size != 50 && size != 100) {
            throw new InvalidPageSizeException(size);
        }

        var productPage = categoryId != null
                ? productQueryService.handle(new GetProductsByCategoryWithPaginationQuery(categoryId, page, size, sortBy, sortDirection))
                : productQueryService.handle(new GetProductsWithPaginationQuery(page, size, sortBy, sortDirection));

        var productResources = productPage.getContent().stream()
                .map(ProductGraphQLResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        var pageMetadata = new PageMetadataGraphQLResource(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.hasNext(),
                productPage.hasPrevious()
        );

        return new ProductPageGraphQLResource(productResources, pageMetadata);
    }
}

