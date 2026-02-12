package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;


@Entity
@Getter
@NoArgsConstructor
public class StripeWebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String stripeEventId;

    @Column(nullable = false, length = 50)
    private String eventType;

    @Column(nullable = false)
    private Date processedAt;

    @Column(length = 200)
    private String stripeSessionId;

    @Column
    private Long orderId;

    @Column(nullable = false)
    private boolean successful;

    @Column(length = 1000)
    private String errorMessage;

    public StripeWebhookEvent(String stripeEventId, String eventType, String stripeSessionId, Long orderId) {
        this.stripeEventId = stripeEventId;
        this.eventType = eventType;
        this.stripeSessionId = stripeSessionId;
        this.orderId = orderId;
        this.processedAt = new Date();
        this.successful = true;
    }

    public void markAsFailed(String errorMessage) {
        this.successful = false;
        this.errorMessage = errorMessage;
    }
}

