package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.DeliveryStatus;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.Discount;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.OrderItem;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.OrderStatus;
import com.finalproject.ecommerce.ecommerce.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn
    private DeliveryStatus deliveryStatus;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 500)
    private String stripeSessionId;

    @Column(length = 1000)
    private String checkoutUrl;

    @Column
    private Instant paidAt;

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
        this.paidAt = Instant.now();
    }

    public void setStripeCheckoutInfo(String sessionId, String checkoutUrl) {
        this.stripeSessionId = sessionId;
        this.checkoutUrl = checkoutUrl;
    }

    public void cancel(OrderStatus cancelledStatus) {
        if (this.status.isPaid()) {
            throw new IllegalStateException("Paid orders cannot be cancelled");
        }

        if (this.deliveryStatus != null && this.deliveryStatus.isDelivered()) {
            throw new IllegalStateException("Cannot cancel order. Order has already been delivered");
        }

        this.status = cancelledStatus;
    }

    public void updateDeliveryStatus(DeliveryStatus newDeliveryStatus) {
        if (!this.status.isPaid()) {
            throw new IllegalStateException("Only paid orders can have their delivery status updated");
        }

        if (this.deliveryStatus != null && this.deliveryStatus.isDelivered()) {
            throw new IllegalStateException("Cannot change delivery status. Order has already been delivered");
        }

        this.deliveryStatus = newDeliveryStatus;
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
