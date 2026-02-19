package com.finalproject.ecommerce.ecommerce.products.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.*;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductQueryService;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public Optional<Product> handle(GetProductByIdQuery query) {
        return productRepository.findById(query.productId());
    }

    @Override
    public List<Product> handle(GetAllProductsQuery query) {
        return productRepository.findByIsDeleted(false);
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
    public Page<Product> handle(GetProductsWithPaginationQuery query) {
        Sort sort = query.sortDirection().equalsIgnoreCase("desc")
                ? Sort.by(query.sortBy()).descending()
                : Sort.by(query.sortBy()).ascending();

        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);

        if (iamContextFacade.currentUserHasRole("ROLE_MANAGER")) {
            return productRepository.findByIsDeleted(false, pageable);
        }

        return productRepository.findByIsDeletedAndIsActive(false, true, pageable);
    }

    @Override
    public Page<Product> handle(GetProductsByCategoryWithPaginationQuery query) {
        Sort sort = query.sortDirection().equalsIgnoreCase("desc")
                ? Sort.by(query.sortBy()).descending()
                : Sort.by(query.sortBy()).ascending();

        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);

        if (iamContextFacade.currentUserHasRole("ROLE_MANAGER")) {
            return productRepository.findDistinctByIsDeletedAndProductCategories_Category_Id(false, query.categoryId(), pageable);
        }

        return productRepository.findDistinctByIsDeletedAndIsActiveAndProductCategories_Category_Id(false, true, query.categoryId(), pageable);
    }
}
