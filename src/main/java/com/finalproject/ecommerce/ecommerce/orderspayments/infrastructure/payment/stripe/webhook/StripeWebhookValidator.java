package com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe.webhook;

import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe.dto.StripeWebhookEventResponse;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Slf4j
@Component
public class StripeWebhookValidator {

    @Value("${stripe.api.secret-key}")
    private String secretKey;

    @Value("${stripe.api.webhook-secret}")
    private String webhookSecret;


    public Optional<StripeWebhookEventResponse> validateAndParseWebhook(String payload, String signature) {
        Stripe.apiKey = secretKey;

        Event event;

        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            throw new RuntimeException("Invalid signature");
        }

        if (event.getType().startsWith("checkout.session.")) {
            return parseCheckoutSessionEvent(event);
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;

        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            log.warn("Deserialization failed for event: {} (type: {})", event.getId(), event.getType());
            log.info("Event type: {}, Event ID: {}", event.getType(), event.getId());
            return Optional.empty();
        }

        log.info("Event type: {}, Event ID: {}", event.getType(), event.getId());
        return Optional.empty();
    }

    private Optional<StripeWebhookEventResponse> parseCheckoutSessionEvent(Event event) {
        try {
            Session session = (Session) event.getData().getObject();

            log.info("=== {} ===", event.getType());
            log.info("Session ID: {}", session.getId());
            log.info("Customer email: {}", session.getCustomerEmail());
            log.info("Payment status: {}", session.getPaymentStatus());
            log.info("Amount total: {} cents", session.getAmountTotal());
            log.info("Currency: {}", session.getCurrency());
            log.info("Order ID (metadata): {}", session.getMetadata().get("order_id"));
            log.info("========================");

            return Optional.of(StripeWebhookEventResponse.builder()
                    .eventType(event.getType())
                    .eventId(event.getId())
                    .sessionId(session.getId())
                    .customerEmail(session.getCustomerEmail())
                    .paymentStatus(session.getPaymentStatus())
                    .amountTotal(session.getAmountTotal())
                    .currency(session.getCurrency())
                    .orderId(session.getMetadata().get("order_id"))
                    .build());
        } catch (Exception e) {
            log.error("Failed to parse checkout session event {}: {}", event.getId(), e.getMessage(), e);
            return Optional.empty();
        }
    }
}

