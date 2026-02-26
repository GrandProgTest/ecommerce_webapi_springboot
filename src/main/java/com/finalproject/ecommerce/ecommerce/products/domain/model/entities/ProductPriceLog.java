package com.finalproject.ecommerce.ecommerce.products.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.valueobjects.PriceType;
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
public class ProductPriceLog extends AuditableAbstractAggregateRoot<ProductPriceLog> {

    @NotNull
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PriceType priceType;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal oldPrice;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal newPrice;

    @Column
    private Instant salePriceExpireDate;

    public ProductPriceLog(Long productId, PriceType priceType, BigDecimal oldPrice, BigDecimal newPrice, Instant salePriceExpireDate) {
        this.productId = productId;
        this.priceType = priceType;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.salePriceExpireDate = salePriceExpireDate;
    }

    public static ProductPriceLog basePriceChange(Long productId, BigDecimal oldPrice, BigDecimal newPrice) {
        return new ProductPriceLog(productId, PriceType.BASE_PRICE, oldPrice, newPrice, null);
    }

    public static ProductPriceLog salePriceChange(Long productId, BigDecimal oldBasePrice, BigDecimal newSalePrice, Instant salePriceExpireDate) {
        return new ProductPriceLog(productId, PriceType.SALE_PRICE, oldBasePrice, newSalePrice, salePriceExpireDate);
    }
}
