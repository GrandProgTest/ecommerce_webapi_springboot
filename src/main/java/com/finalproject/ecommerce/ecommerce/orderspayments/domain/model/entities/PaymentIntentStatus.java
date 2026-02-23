package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.PaymentIntentStatuses;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class PaymentIntentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false, length = 50)
    private PaymentIntentStatuses name;

    @Column(length = 500)
    private String description;

    public PaymentIntentStatus() {
    }

    public PaymentIntentStatus(PaymentIntentStatuses name, String description) {
        this.name = name;
        this.description = description;
    }

    public boolean isSucceeded() {
        return this.name == PaymentIntentStatuses.SUCCEEDED;
    }

    public boolean isCanceled() {
        return this.name == PaymentIntentStatuses.CANCELED;
    }

    public boolean isPending() {
        return this.name == PaymentIntentStatuses.REQUIRES_PAYMENT_METHOD ||
               this.name == PaymentIntentStatuses.REQUIRES_CONFIRMATION ||
               this.name == PaymentIntentStatuses.REQUIRES_ACTION ||
               this.name == PaymentIntentStatuses.PROCESSING ||
               this.name == PaymentIntentStatuses.REQUIRES_CAPTURE;
    }

    public boolean isFailed() {
        return false;
    }
}

