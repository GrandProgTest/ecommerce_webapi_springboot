package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.PaymentIntentStatuses;
import com.finalproject.ecommerce.ecommerce.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "payment_intent", indexes = {
        @Index(name = "idx_payment_intent_stripe_id", columnList = "stripePaymentIntentId"),
        @Index(name = "idx_payment_intent_order_id", columnList = "order_id")
})
public class PaymentIntent extends AuditableModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @NotBlank
    @Column(nullable = false, unique = true, length = 500)
    private String stripePaymentIntentId;

    @Column(length = 1000)
    private String clientSecret;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(nullable = false)
    private PaymentIntentStatus status;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 10)
    private String currency;

    @Column(length = 1000)
    private String failureReason;

    public PaymentIntent() {
    }

    public PaymentIntent(Order order, String stripePaymentIntentId, String clientSecret, PaymentIntentStatus status, BigDecimal amount, String currency) {
        this.order = order;
        this.stripePaymentIntentId = stripePaymentIntentId;
        this.clientSecret = clientSecret;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
    }

    public static PaymentIntent fromStripeResponse(Order order, String paymentIntentId, String clientSecret, PaymentIntentStatus status, BigDecimal amount, String currency) {
        return new PaymentIntent(order, paymentIntentId, clientSecret, status, amount, currency);
    }

    public void updateStatus(PaymentIntentStatus newStatus) {
        this.status = newStatus;
    }

    public void markAsFailed(PaymentIntentStatus failedStatus, String reason) {
        this.status = failedStatus;
        this.failureReason = reason;
    }

    public boolean isSucceeded() {
        return this.status.isSucceeded();
    }

    public boolean isCanceled() {
        return this.status.isCanceled();
    }

    public boolean isFailed() {
        return this.status.isFailed();
    }

    public boolean isPending() {
        return this.status.isPending();
    }
}

