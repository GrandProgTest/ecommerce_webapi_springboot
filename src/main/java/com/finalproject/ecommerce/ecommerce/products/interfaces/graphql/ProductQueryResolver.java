package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql;

import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductByIdQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductsWithPaginationQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductQueryService;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.mapper.ProductGraphQLMapper;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.mapper.ProductGraphQLMapper.*;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.InvalidPageSizeException;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ProductQueryResolver {

    private final ProductQueryService productQueryService;
    private final IamContextFacade iamContextFacade;

    @QueryMapping(name = "getProductById")
    public ProductGraphQLResource getProductById(@Argument Long id) {
        var query = new GetProductByIdQuery(id);
        var product = productQueryService.handle(query)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ProductGraphQLMapper.toResource(product);
    }

    @QueryMapping(name = "getAllProducts")
    public ProductPageGraphQLResource getAllProducts(
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

        boolean isManager = iamContextFacade.currentUserHasRole("ROLE_MANAGER");

        var productPageResponse = productQueryService.handleForGraphQL(
                new GetProductsWithPaginationQuery(categoryId, null, page, size, sortBy, sortDirection),
                isManager
        );

        var productResources = productPageResponse.products().stream()
                .map(p -> new ProductGraphQLResource(
                        p.id(), p.name(), p.description(),
                        p.price() != null ? p.price().doubleValue() : null,
                        p.salePrice() != null ? p.salePrice().doubleValue() : null,
                        p.salePriceExpireDate(),
                        p.effectivePrice() != null ? p.effectivePrice().doubleValue() : null,
                        p.hasActiveSalePrice(),
                        p.stock(),
                        p.isActive(),
                        p.categoryIds(),
                        p.createdByUserId(),
                        p.likeCount(),
                        null,
                        p.stock() != null && p.stock() > 0,
                        p.primaryImageUrl(),
                        p.createdAt(),
                        p.updatedAt()
                ))
                .toList();

        var meta = productPageResponse.pageMetadata();
        var pageMetadata = new PageMetadataGraphQLResource(
                meta.currentPage(), meta.pageSize(), meta.totalElements(),
                meta.totalPages(), meta.hasNext(), meta.hasPrevious()
        );

        return new ProductPageGraphQLResource(productResources, pageMetadata);
    }
}
