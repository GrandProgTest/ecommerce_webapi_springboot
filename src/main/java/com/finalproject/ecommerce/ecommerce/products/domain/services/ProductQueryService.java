package com.finalproject.ecommerce.ecommerce.products.domain.services;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface ProductQueryService {
    Optional<Product> handle(GetProductByIdQuery query);

    List<Product> handle(GetAllProductsQuery query);

    List<Product> handle(GetActiveProductsQuery query);

    List<Product> handle(GetProductsByIdsQuery query);

    Page<Product> handle(GetProductsWithPaginationQuery query);
}
