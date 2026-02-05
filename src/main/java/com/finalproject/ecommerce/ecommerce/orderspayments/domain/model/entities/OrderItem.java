package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, name = "product_id")
    private Long productId;

    @Column(nullable = false, name = "price_at_purchase", precision = 10, scale = 2)
    private BigDecimal priceAtPurchase;

    @Column(nullable = false)
    private Integer quantity;

    protected OrderItem() {
    }

    public OrderItem(Order order, Long productId, BigDecimal priceAtPurchase, Integer quantity) {
        this.order = order;
        this.productId = productId;
        this.priceAtPurchase = priceAtPurchase;
        this.quantity = quantity;
        validateOrderItem();
    }

    private void validateOrderItem() {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (priceAtPurchase == null || priceAtPurchase.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be non-negative");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public BigDecimal getSubtotal() {
        return priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }
}
