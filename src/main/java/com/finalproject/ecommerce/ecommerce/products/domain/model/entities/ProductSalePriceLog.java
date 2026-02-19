package com.finalproject.ecommerce.ecommerce.products.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@NoArgsConstructor
@Entity
public class ProductSalePriceLog extends AuditableAbstractAggregateRoot<ProductSalePriceLog> {

    @NotNull
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal salePrice;

    @NotNull
    @Column(nullable = false)
    private Instant salePriceExpireDate;

    public ProductSalePriceLog(Long productId, BigDecimal basePrice, BigDecimal salePrice, Instant salePriceExpireDate) {
        this.productId = productId;
        this.basePrice = basePrice;
        this.salePrice = salePrice;
        this.salePriceExpireDate = salePriceExpireDate;
    }
}

