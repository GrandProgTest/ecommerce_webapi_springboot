package com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe;

import com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.outboundservices.payment.StripeService;
import com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.outboundservices.payment.dto.StripeResponse;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.products.interfaces.acl.ProductContextFacade;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class StripePaymentServiceImpl implements StripeService {

    private final ProductContextFacade productContextFacade;

    @Value("${stripe.api.secret-key}")
    private String secretKey;

    @Value("${stripe.api.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @Value("${stripe.currency:usd}")
    private String currency;

    public StripePaymentServiceImpl(ProductContextFacade productContextFacade) {
        this.productContextFacade = productContextFacade;
    }

    @Override
    public StripeResponse createCheckoutSession(Order order) {
        Stripe.apiKey = secretKey;


        try {
            List<Long> productIds = order.getItems().stream()
                    .map(orderItem -> orderItem.getProductId())
                    .collect(Collectors.toList());

            Map<Long, String> productNames = productContextFacade.getProductNames(productIds);

            log.debug("Fetched {} product names for Stripe checkout", productNames.size());

            List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

            order.getItems().forEach(orderItem -> {
                long unitAmountInCents = orderItem.getPriceAtPurchase()
                        .multiply(BigDecimal.valueOf(100))
                        .longValue();

                String productName = productNames.getOrDefault(
                        orderItem.getProductId(),
                        "Product ID: " + orderItem.getProductId()
                );

                SessionCreateParams.LineItem.PriceData.ProductData productData =
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(productName)
                                .build();

                SessionCreateParams.LineItem.PriceData priceData =
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(currency)
                                .setUnitAmount(unitAmountInCents)
                                .setProductData(productData)
                                .build();

                SessionCreateParams.LineItem lineItem =
                        SessionCreateParams.LineItem.builder()
                                .setQuantity((long) orderItem.getQuantity())
                                .setPriceData(priceData)
                                .build();

                lineItems.add(lineItem);

                log.debug("Added line item: {} - Qty: {}, Price: {}",
                        productName, orderItem.getQuantity(), orderItem.getPriceAtPurchase());
            });

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addAllLineItem(lineItems)
                    .putMetadata("order_id", order.getId().toString())
                    .putMetadata("user_id", order.getUserId().toString())
                    .setClientReferenceId(order.getId().toString())
                    .setExpiresAt(System.currentTimeMillis() / 1000 + 1800)
                    .build();

            Session session = Session.create(params);

            log.info("Stripe checkout session created - Order ID: {}, Session ID: {}", order.getId(), session.getId());

            return StripeResponse.builder()
                    .status("SUCCESS")
                    .message("Payment session created")
                    .sessionId(session.getId())
                    .checkoutUrl(session.getUrl())
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error during checkout: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating Stripe checkout session: " + e.getMessage(), e);
        }
    }

    @Override
    public String handleWebhookEvent(String payload, String signature) {
        Stripe.apiKey = secretKey;

        Event event;

        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook signature verification failed: {}", e.getMessage());
            throw new RuntimeException("Invalid signature");
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;

        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            log.warn("Deserialization failed for event: {} (type: {})", event.getId(), event.getType());
            log.info("Event type: {}, Event ID: {}", event.getType(), event.getId());
            return event.getType();
        }

        Session session;
        switch (event.getType()) {
            case "checkout.session.completed":
                if (stripeObject instanceof Session) {
                    session = (Session) stripeObject;
                    log.info("=== Payment Completed ===");
                    log.info("Session ID: {}", session.getId());
                    log.info("Customer email: {}", session.getCustomerEmail());
                    log.info("Payment status: {}", session.getPaymentStatus());
                    log.info("Amount total: {} cents", session.getAmountTotal());
                    log.info("Currency: {}", session.getCurrency());
                    log.info("Order ID (metadata): {}", session.getMetadata().get("order_id"));
                    log.info("========================");

                    // TODO: Trigger domain event or call command service to mark order as paid
                } else {
                    log.error("Expected Session object but got: {}",
                            stripeObject != null ? stripeObject.getClass().getName() : "null");
                }
                break;

            case "checkout.session.async_payment_succeeded":
                if (stripeObject instanceof Session) {
                    session = (Session) stripeObject;
                    log.info("Async payment succeeded! Session ID: {}", session.getId());
                } else {
                    log.error("Expected Session object but got: {}",
                            stripeObject != null ? stripeObject.getClass().getName() : "null");
                }
                break;

            case "checkout.session.async_payment_failed":
                if (stripeObject instanceof Session) {
                    session = (Session) stripeObject;
                    log.warn("Async payment failed! Session ID: {}", session.getId());
                } else {
                    log.error("Expected Session object but got: {}",
                            stripeObject != null ? stripeObject.getClass().getName() : "null");
                }
                break;

            case "payment_intent.succeeded":
                log.info("PaymentIntent succeeded: {}", event.getId());
                break;

            case "payment_intent.payment_failed":
                log.warn("PaymentIntent failed: {}", event.getId());
                break;

            default:
                log.info("Unhandled event type: {}", event.getType());
        }

        return event.getType();
    }

    @Override
    public String getPaymentStatus(String sessionId) {
        Stripe.apiKey = secretKey;

        try {
            Session session = Session.retrieve(sessionId);
            log.info("Retrieved payment status for session {}: {}", sessionId, session.getPaymentStatus());
            return session.getPaymentStatus();
        } catch (StripeException e) {
            log.error("Error retrieving payment status for session ID: {}", sessionId, e);
            throw new RuntimeException("Failed to retrieve payment status: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelPaymentSession(String sessionId) {
        Stripe.apiKey = secretKey;

        try {
            Session session = Session.retrieve(sessionId);
            String currentStatus = session.getStatus();

            log.info("Attempting to cancel Stripe session {} with current status: {}", sessionId, currentStatus);

            switch (currentStatus) {
                case "open" -> {
                    session.expire();
                    log.info("Stripe session {} expired successfully", sessionId);
                }
                case "complete" -> {
                    log.warn("Cannot cancel session {} - payment already completed", sessionId);
                    throw new IllegalStateException("Cannot cancel a completed payment session");
                }
                case "expired" -> log.info("Stripe session {} is already expired", sessionId);
                default -> log.warn("Cannot cancel session {} - current status: {}", sessionId, currentStatus);
            }
        } catch (StripeException e) {
            log.error("Error cancelling payment session ID: {}", sessionId, e);
            throw new RuntimeException("Failed to cancel payment session: " + e.getMessage(), e);
        }
    }
}
