package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/stripe")
public class StripeWebhookController {

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String stripeSignature
    ) {
        log.info("Hello World - Webhook received from Stripe!");
        log.info("Payload length: {} bytes", payload != null ? payload.length() : 0);
        log.info("Stripe-Signature header present: {}", stripeSignature != null);

        return ResponseEntity.ok("Hello World - Webhook received successfully");
    }
}
