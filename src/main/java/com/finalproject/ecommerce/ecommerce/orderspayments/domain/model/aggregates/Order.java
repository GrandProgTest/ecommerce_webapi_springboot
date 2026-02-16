package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.Discount;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.OrderItem;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.OrderStatus;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.OrderStatuses;
import com.finalproject.ecommerce.ecommerce.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
public class Order extends AuditableAbstractAggregateRoot<Order> {

    @Column(nullable = false)
    private Long userId;

    @Column(unique = true)
    private Long cartId;

    @Column(nullable = false)
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Discount discount;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 500)
    private String stripeSessionId;

    @Column(length = 1000)
    private String checkoutUrl;

    @Column
    private Date paidAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();


    protected Order() {
    }

    public Order(Long userId, Long cartId, Long addressId, OrderStatus status) {
        this.userId = userId;
        this.cartId = cartId;
        this.addressId = addressId;
        this.status = status;
        this.totalAmount = BigDecimal.ZERO;
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

    // Will be refactored following products discount codes and discount prices
    private void recalculateTotal() {
        BigDecimal subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = subtotal;
    }

    public void markAsPaid(OrderStatus paidStatus) {
        if (!this.status.isPending()) {
            throw new IllegalStateException("Only pending orders can be marked as paid");
        }
        this.status = paidStatus;
        this.paidAt = new Date();
    }

    public void setStripeCheckoutInfo(String sessionId, String checkoutUrl) {
        this.stripeSessionId = sessionId;
        this.checkoutUrl = checkoutUrl;
    }

    public void cancel(OrderStatus cancelledStatus) {
        if (this.status.isPaid()) {
            throw new IllegalStateException("Paid orders cannot be cancelled");
        }

        OrderStatuses currentStatusEnum = this.status.toEnum();
        if (currentStatusEnum == OrderStatuses.SHIPPED || currentStatusEnum == OrderStatuses.DELIVERED) {
            throw new IllegalStateException(
                "Cannot cancel order. Order is already " + currentStatusEnum.name()
            );
        }

        this.status = cancelledStatus;
    }

    public void updateStatus(OrderStatus newStatus) {
        if (!this.status.isPaid()) {
            throw new IllegalStateException("Only paid orders can have their delivery status updated");
        }

        OrderStatuses newStatusEnum = newStatus.toEnum();
        if (newStatusEnum != OrderStatuses.SHIPPED && newStatusEnum != OrderStatuses.DELIVERED) {
            throw new IllegalArgumentException("Can only update to SHIPPED or DELIVERED status");
        }

        OrderStatuses currentStatusEnum = this.status.toEnum();

        if (currentStatusEnum == OrderStatuses.DELIVERED) {
            throw new IllegalStateException(
                "Cannot change status from DELIVERED. Order has already been delivered."
            );
        }

        if (currentStatusEnum == OrderStatuses.SHIPPED && newStatusEnum == OrderStatuses.PAID) {
            throw new IllegalStateException(
                "Cannot change status back to PAID from SHIPPED"
            );
        }

        if (currentStatusEnum == newStatusEnum) {
            return;
        }

        this.status = newStatus;
    }

    public boolean isPending() {
        return this.status.isPending();
    }

    public boolean isPaid() {
        return this.status.isPaid();
    }

    public boolean isCancelled() {
        return this.status.isCancelled();
    }
}
