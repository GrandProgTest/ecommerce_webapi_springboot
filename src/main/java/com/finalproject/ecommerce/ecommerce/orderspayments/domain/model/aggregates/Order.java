package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.Discount;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.OrderItem;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.OrderStatuses;
import com.finalproject.ecommerce.ecommerce.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "\"order\"")
public class Order extends AuditableAbstractAggregateRoot<Order> {

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @Column(unique = true, name = "cart_id")
    private Long cartId;

    @Column(nullable = false, name = "address_id")
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id")
    private Discount discount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatuses status;

    @Column(nullable = false, name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {
    }

    public Order(Long userId, Long cartId, Long addressId) {
        this.userId = userId;
        this.cartId = cartId;
        this.addressId = addressId;
        this.status = OrderStatuses.PENDING;
        this.totalAmount = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
    }

    public void addItem(Long productId, BigDecimal price, Integer quantity) {
        OrderItem item = new OrderItem(this, productId, price, quantity);
        this.items.add(item);
        recalculateTotal();
    }

    public void applyDiscount(Discount discount) {
        if (discount == null) {
            return;
        }
        if (!discount.isValid()) {
            throw new IllegalArgumentException("Discount is not valid or has expired");
        }
        this.discount = discount;
        recalculateTotal();
    }

    private void recalculateTotal() {
        BigDecimal subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (discount != null && discount.isValid()) {
            this.discountAmount = subtotal
                    .multiply(BigDecimal.valueOf(discount.getPercentage()))
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        } else {
            this.discountAmount = BigDecimal.ZERO;
        }

        this.totalAmount = subtotal.subtract(this.discountAmount);
    }

    public void markAsPaid() {
        if (this.status != OrderStatuses.PENDING) {
            throw new IllegalStateException("Only pending orders can be marked as paid");
        }
        this.status = OrderStatuses.PAID;
    }

    public void cancel() {
        if (this.status == OrderStatuses.PAID) {
            throw new IllegalStateException("Paid orders cannot be cancelled");
        }
        this.status = OrderStatuses.CANCELLED;
    }

    public boolean isPending() {
        return this.status == OrderStatuses.PENDING;
    }

    public boolean isPaid() {
        return this.status == OrderStatuses.PAID;
    }

    public boolean isCancelled() {
        return this.status == OrderStatuses.CANCELLED;
    }
}
