package com.finalproject.ecommerce.ecommerce.products.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.products.application.dto.PageMetadataResponse;
import com.finalproject.ecommerce.ecommerce.products.application.dto.ProductPageResponse;
import com.finalproject.ecommerce.ecommerce.products.application.dto.ProductResponse;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.*;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductQueryService;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductSpecification;
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
    private final IamContextFacade iamContextFacade;

    public ProductQueryServiceImpl(ProductRepository productRepository, IamContextFacade iamContextFacade) {
        this.productRepository = productRepository;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    //@Cacheable(value = "productById", key = "#query.productId()")
    public Optional<Product> handle(GetProductByIdQuery query) {
        return productRepository.findById(query.productId());
    }

    @Override
    //@Cacheable(value = "activeProducts", key = "'active'")
    public List<Product> handle(GetActiveProductsQuery query) {
        return productRepository.findByIsDeletedAndIsActive(false, true);
    }

    @Override
    //@Cacheable(value = "productsByIds", key = "#query.productIds().toString()")
    public List<Product> handle(GetProductsByIdsQuery query) {
        if (query.productIds() == null || query.productIds().isEmpty()) {
            return List.of();
        }
        return productRepository.findAllById(query.productIds()).stream()
                .filter(product -> !product.getIsDeleted())
                .toList();
    }

    @Override
    public ProductPageResponse handle(GetProductsWithPaginationQuery query) {
        boolean isManager = iamContextFacade.currentUserHasRole("ROLE_MANAGER");
        Boolean activeFilter = isManager ? query.isActive() : true;

        return findProductsCached(
                query.categoryId(), activeFilter, query.page(),
                query.size(), query.sortBy(), query.sortDirection(), isManager
        );
    }

    @Cacheable(
            value = "productsPage",
            key = "'role_' + (#isManager ? 'manager' : 'customer')" +
                  " + '_cat_' + (#categoryId != null ? #categoryId : 'all')" +
                  " + '_active_' + (#activeFilter != null ? #activeFilter : 'any')" +
                  " + '_page_' + #page" +
                  " + '_size_' + #size" +
                  " + '_sort_' + #sortBy + '_' + #sortDirection"
    )
    public ProductPageResponse findProductsCached(Long categoryId, Boolean activeFilter, int page,
                                                   int size, String sortBy, String sortDirection,
                                                   boolean isManager) {
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        var productPage = productRepository.findAll(
                ProductSpecification.withFilters(categoryId, activeFilter, false),
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
}


