package com.finalproject.ecommerce.ecommerce.products.application.acl.services;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DecreaseProductStockCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.IncreaseProductStockCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductByIdQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.GetProductsByIdsQuery;
import com.finalproject.ecommerce.ecommerce.products.domain.services.ProductCommandService;
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
    private final ProductCommandService productCommandService;

    public ProductContextFacadeImpl(ProductQueryService productQueryService, ProductCommandService productCommandService) {
        this.productQueryService = productQueryService;
        this.productCommandService = productCommandService;
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
    public boolean isProductDeleted(Long productId) {
        var query = new GetProductByIdQuery(productId);
        var productOpt = productQueryService.handle(query);

        return productOpt.map(Product::getIsDeleted).orElse(true);
    }

    @Override
    public BigDecimal getProductPrice(Long productId) {
        var query = new GetProductByIdQuery(productId);
        var productOpt = productQueryService.handle(query);

        return productOpt.map(Product::getEffectivePrice).orElse(null);
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
    public void decreaseProductStock(Long productId, Integer quantity) {
        var command = new DecreaseProductStockCommand(productId, quantity);
        productCommandService.handle(command);
    }

    @Override
    public void increaseProductStock(Long productId, Integer quantity) {
        var command = new IncreaseProductStockCommand(productId, quantity);
        productCommandService.handle(command);
    }

    @Override
    public List<Long> getUsersWhoLikedProduct(Long productId) {
        var query = new GetProductByIdQuery(productId);
        var productOpt = productQueryService.handle(query);

        return productOpt.map(Product::getLikedByUserIds).orElse(List.of());
    }
}
