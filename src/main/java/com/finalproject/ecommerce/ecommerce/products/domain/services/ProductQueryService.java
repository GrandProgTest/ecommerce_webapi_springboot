package com.finalproject.ecommerce.ecommerce.products.domain.services;

import com.finalproject.ecommerce.ecommerce.products.application.dto.ProductPageResponse;
import com.finalproject.ecommerce.ecommerce.products.application.dto.ProductPageResponseGraphQL;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface ProductQueryService {
    Optional<Product> handle(GetProductByIdQuery query);

    List<Product> handle(GetActiveProductsQuery query);

    List<Product> handle(GetProductsByIdsQuery query);

    ProductPageResponse handle(GetProductsWithPaginationQuery query);

    ProductPageResponseGraphQL handleForGraphQL(GetProductsWithPaginationQuery query);
}
