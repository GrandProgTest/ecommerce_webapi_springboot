package com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe.webhook;

import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe.dto.StripeWebhookEventResponse;
import com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration.properties.StripeProperties;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class StripeWebhookValidator {

    private final StripeProperties stripeProperties;

    public Optional<StripeWebhookEventResponse> validateAndParseWebhook(String payload, String signature) {
        Stripe.apiKey = stripeProperties.getApi().getSecretKey();

        Event event;

        try {
            event = Webhook.constructEvent(payload, signature, stripeProperties.getApi().getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            throw new RuntimeException("Invalid signature");
        }

        if (event.getType().startsWith("payment_intent.")) {
            return parsePaymentIntentEvent(event);
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

    private Optional<StripeWebhookEventResponse> parsePaymentIntentEvent(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getData().getObject();

            return Optional.of(StripeWebhookEventResponse.builder()
                    .eventType(event.getType())
                    .eventId(event.getId())
                    .paymentIntentId(paymentIntent.getId())
                    .paymentStatus(paymentIntent.getStatus())
                    .amountTotal(paymentIntent.getAmount())
                    .currency(paymentIntent.getCurrency())
                    .orderId(paymentIntent.getMetadata().get("order_id"))
                    .build());
        } catch (Exception e) {
            log.error("Failed to parse payment intent event {}: {}", event.getId(), e.getMessage(), e);
            return Optional.empty();
        }
    }
}

