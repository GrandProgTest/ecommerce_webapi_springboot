package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe.webhook.StripeWebhookEventHandler;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe.webhook.StripeWebhookValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/stripe")
public class StripeWebhookController {

    private final StripeWebhookValidator webhookValidator;
    private final StripeWebhookEventHandler webhookEventHandler;

    public StripeWebhookController(StripeWebhookValidator webhookValidator,
                                   StripeWebhookEventHandler webhookEventHandler) {
        this.webhookValidator = webhookValidator;
        this.webhookEventHandler = webhookEventHandler;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature") String stripeSignature
    ) {
        log.info("Webhook received from Stripe");
        log.debug("Payload length: {} bytes", payload.length());

        try {
            var webhookEventOpt = webhookValidator.validateAndParseWebhook(payload, stripeSignature);

            if (webhookEventOpt.isEmpty()) {
                log.info("Webhook event processed but no action required");
                return ResponseEntity.ok("Webhook received");
            }

            var webhookEvent = webhookEventOpt.get();
            log.info("Processing Stripe event: {}", webhookEvent.getEventType());

            switch (webhookEvent.getEventType()) {
                case "checkout.session.completed":
                    webhookEventHandler.handleCheckoutSessionCompleted(
                        webhookEvent.getEventId(),
                        webhookEvent.getSessionId()
                    );
                    break;

                case "checkout.session.async_payment_succeeded":
                    webhookEventHandler.handleAsyncPaymentSucceeded(
                        webhookEvent.getEventId(),
                        webhookEvent.getSessionId()
                    );
                    break;

                case "checkout.session.async_payment_failed":
                    webhookEventHandler.handleAsyncPaymentFailed(
                        webhookEvent.getEventId(),
                        webhookEvent.getSessionId()
                    );
                    break;

                default:
                    log.info("Unhandled event type in controller: {}", webhookEvent.getEventType());
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (RuntimeException e) {
            log.error("Error processing Stripe webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Webhook processing failed: " + e.getMessage());
        }
    }
}

