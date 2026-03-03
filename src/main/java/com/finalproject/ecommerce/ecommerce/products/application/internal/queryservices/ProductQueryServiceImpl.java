package com.finalproject.ecommerce.ecommerce.products.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.products.application.dto.*;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.*;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductQueryService;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.specifications.ProductSpecification;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;

    public ProductQueryServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Optional<Product> handle(GetProductByIdQuery query) {
        return productRepository.findById(query.productId());
    }

    @Override
    public List<Product> handle(GetActiveProductsQuery query) {
        return productRepository.findByIsDeletedAndIsActive(false, true);
    }

    @Override
    public List<Product> handle(GetProductsByIdsQuery query) {
        if (query.productIds() == null || query.productIds().isEmpty()) {
            return List.of();
        }
        return productRepository.findAllById(query.productIds()).stream()
                .filter(product -> !product.getIsDeleted())
                .toList();
    }

    @Override
    @Cacheable(
            value = "productsPage",
            key = "'role_' + (#isManager ? 'ROLE_MANAGER' : 'ROLE_CLIENT')" +
                  " + '_cat_' + (#query.categoryId() != null ? #query.categoryId() : 'all')" +
                  " + '_active_' + (#query.isActive() != null ? #query.isActive() : 'any')" +
                  " + '_page_' + #query.page()" +
                  " + '_size_' + #query.size()" +
                  " + '_sort_' + #query.sortBy() + '_' + #query.sortDirection()"
    )
    public ProductPageResponse handle(GetProductsWithPaginationQuery query, boolean isManager) {
        Boolean activeFilter = isManager ? query.isActive() : Boolean.TRUE;

        Sort sort = query.sortDirection().equalsIgnoreCase("desc")
                ? Sort.by(query.sortBy()).descending()
                : Sort.by(query.sortBy()).ascending();

        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);

        var productPage = productRepository.findAll(
                ProductSpecification.withFilters(query.categoryId(), activeFilter, false),
                pageable
        );


        var products = productPage.getContent().stream()
                .map(ProductResponse::fromEntity)
                .toList();

        var metadata = new PageMetadataResponse(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.hasNext(),
                productPage.hasPrevious()
        );

        return new ProductPageResponse(products, metadata);
    }

    @Override
    @Cacheable(
            value = "productsPageGraphQL",
            key = "'role_' + (#isManager ? 'ROLE_MANAGER' : 'ROLE_CLIENT')" +
                  " + '_cat_' + (#query.categoryId() != null ? #query.categoryId() : 'all')" +
                  " + '_active_' + (#query.isActive() != null ? #query.isActive() : 'any')" +
                  " + '_page_' + #query.page()" +
                  " + '_size_' + #query.size()" +
                  " + '_sort_' + #query.sortBy() + '_' + #query.sortDirection()"
    )
    public ProductPageResponseGraphQL handleForGraphQL(GetProductsWithPaginationQuery query, boolean isManager) {
        Boolean activeFilter = isManager ? query.isActive() : Boolean.TRUE;

        Sort sort = query.sortDirection().equalsIgnoreCase("desc")
                ? Sort.by(query.sortBy()).descending()
                : Sort.by(query.sortBy()).ascending();

        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);

        var productPage = productRepository.findAll(
                ProductSpecification.withFilters(query.categoryId(), activeFilter, false),
                pageable
        );


        var products = productPage.getContent().stream()
                .map(ProductResponseGraphQL::fromEntity)
                .toList();

        var metadata = new PageMetadataResponse(
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.hasNext(),
                productPage.hasPrevious()
        );

        return new ProductPageResponseGraphQL(products, metadata);
    }
}

