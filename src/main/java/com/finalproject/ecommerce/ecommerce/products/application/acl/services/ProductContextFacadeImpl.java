package com.finalproject.ecommerce.ecommerce.products.application.acl.services;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductByIdQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductsByIdsQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductQueryService;
import com.finalproject.ecommerce.ecommerce.products.interfaces.acl.ProductContextFacade;
import com.finalproject.ecommerce.ecommerce.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ProductContextFacadeImpl implements ProductContextFacade {

    private final ProductQueryService productQueryService;

    public ProductContextFacadeImpl(ProductQueryService productQueryService) {
        this.productQueryService = productQueryService;
    }

    @Override
    public boolean productExists(Long productId) {
        var query = new GetProductByIdQuery(productId);
        return productQueryService.handle(query).isPresent();
    }

    @Override
    public boolean hasAvailableStock(Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return false;
        }

        var query = new GetProductByIdQuery(productId);
        var productOpt = productQueryService.handle(query);

        return productOpt.map(product -> product.getStock() >= quantity).orElse(false);
    }

    @Override
    public Integer getProductStock(Long productId) {
        var query = new GetProductByIdQuery(productId);
        var productOpt = productQueryService.handle(query);

        return productOpt.map(Product::getStock).orElse(0);
    }

    @Override
    public boolean isProductActive(Long productId) {
        var query = new GetProductByIdQuery(productId);
        var productOpt = productQueryService.handle(query);

        return productOpt.map(Product::getIsActive).orElse(false);
    }

    @Override
    public BigDecimal getProductPrice(Long productId) {
        var query = new GetProductByIdQuery(productId);
        var productOpt = productQueryService.handle(query);

        return productOpt.map(Product::getPriceAmount).orElse(null);
    }

    @Override
    public String getProductName(Long productId) {
        var query = new GetProductByIdQuery(productId);
        var productOpt = productQueryService.handle(query);

        return productOpt.map(Product::getName).orElse("");
    }

    @Override
    public Map<Long, String> getProductNames(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        var query = new GetProductsByIdsQuery(productIds);
        var products = productQueryService.handle(query);

        return products.stream()
                .collect(Collectors.toMap(
                        AuditableAbstractAggregateRoot::getId,
                        Product::getName
                ));
    }

    @Override
    public boolean isProductAvailableForPurchase(Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return false;
        }

        var query = new GetProductByIdQuery(productId);
        var productOpt = productQueryService.handle(query);

        return productOpt.map(product -> product.getIsActive() && product.getStock() >= quantity).orElse(false);
    }
}
