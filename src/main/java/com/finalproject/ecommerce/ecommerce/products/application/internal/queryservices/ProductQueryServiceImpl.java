package com.finalproject.ecommerce.ecommerce.products.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.*;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductQueryService;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductSpecification;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository productRepository;
    private final IamContextFacade iamContextFacade;

    public ProductQueryServiceImpl(ProductRepository productRepository, IamContextFacade iamContextFacade) {
        this.productRepository = productRepository;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    @Cacheable(value = "productById", key = "#query.productId()")
    public Optional<Product> handle(GetProductByIdQuery query) {
        return productRepository.findById(query.productId());
    }

    @Override
    @Cacheable(value = "allProducts", key = "'all'")
    public List<Product> handle(GetAllProductsQuery query) {
        return productRepository.findByIsDeleted(false);
    }

    @Override
    @Cacheable(value = "activeProducts", key = "'active'")
    public List<Product> handle(GetActiveProductsQuery query) {
        return productRepository.findByIsDeletedAndIsActive(false, true);
    }

    @Override
    @Cacheable(value = "productsByIds", key = "#query.productIds().toString()")
    public List<Product> handle(GetProductsByIdsQuery query) {
        if (query.productIds() == null || query.productIds().isEmpty()) {
            return List.of();
        }
        return productRepository.findAllById(query.productIds()).stream()
                .filter(product -> !product.getIsDeleted())
                .toList();
    }

    @Override
    @Cacheable(value = "productsPage",
            key = "'cat_' + (#query.categoryId() != null ? #query.categoryId() : 'all') + '_active_' + (#query.isActive() != null ? #query.isActive() : 'any') + '_page_' + #query.page() + '_size_' + #query.size() + '_sort_' + #query.sortBy() + '_' + #query.sortDirection()")
    public Page<Product> handle(GetProductsWithPaginationQuery query) {
        Sort sort = query.sortDirection().equalsIgnoreCase("desc")
                ? Sort.by(query.sortBy()).descending()
                : Sort.by(query.sortBy()).ascending();

        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);

        boolean isManager = iamContextFacade.currentUserHasRole("ROLE_MANAGER");

        Boolean activeFilter = isManager ? query.isActive() : true;

        return productRepository.findAll(
                ProductSpecification.withFilters(
                        query.categoryId(),
                        activeFilter,
                        false
                ),
                pageable
        );
    }
}
