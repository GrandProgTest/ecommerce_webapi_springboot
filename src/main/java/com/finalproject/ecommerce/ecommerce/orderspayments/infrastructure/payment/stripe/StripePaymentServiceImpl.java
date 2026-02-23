package com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe;

import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe.dto.PaymentIntentResponse;
import com.finalproject.ecommerce.ecommerce.orderspayments.application.ports.out.PaymentProvider;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.products.interfaces.acl.ProductContextFacade;
import com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration.properties.StripeProperties;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StripePaymentServiceImpl implements PaymentProvider {

    private final ProductContextFacade productContextFacade;
    private final StripeProperties stripeProperties;

    public StripePaymentServiceImpl(ProductContextFacade productContextFacade, StripeProperties stripeProperties) {
        this.productContextFacade = productContextFacade;
        this.stripeProperties = stripeProperties;
    }

    @Override
    public PaymentIntentResponse initiatePayment(Order order) {
        Stripe.apiKey = stripeProperties.getApi().getSecretKey();

        try {
            long amountInCents = order.getTotalAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            List<Long> productIds = order.getItems().stream()
                    .map(orderItem -> orderItem.getProductId())
                    .collect(Collectors.toList());

            Map<Long, String> productNames = productContextFacade.getProductNames(productIds);

            String description = order.getItems().stream()
                    .map(item -> {
                        String productName = productNames.getOrDefault(
                                item.getProductId(),
                                "Product ID: " + item.getProductId()
                        );
                        return String.format("%s (x%d)", productName, item.getQuantity());
                    })
                    .collect(Collectors.joining(", "));

            Map<String, String> metadata = new HashMap<>();
            metadata.put("order_id", order.getId().toString());
            metadata.put("user_id", order.getUserId().toString());
            metadata.put("cart_id", order.getCartId().toString());

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(stripeProperties.getCurrency())
                    .setDescription(description)
                    .putAllMetadata(metadata)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            log.info("Payment Intent created successfully for Order {}: {}", order.getId(), paymentIntent.getId());

            return new PaymentIntentResponse(
                    paymentIntent.getId(),
                    paymentIntent.getClientSecret(),
                    paymentIntent.getStatus(),
                    "Payment Intent created successfully"
            );

        } catch (StripeException e) {
            log.error("Stripe error during payment intent creation: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating Stripe Payment Intent: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelPayment(String paymentIntentId) {
        Stripe.apiKey = stripeProperties.getApi().getSecretKey();

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            String currentStatus = paymentIntent.getStatus();

            switch (currentStatus) {
                case "requires_payment_method", "requires_confirmation", "requires_action" -> {
                    paymentIntent.cancel();
                    log.info("Payment Intent {} cancelled successfully", paymentIntentId);
                }
                case "succeeded" -> throw new IllegalStateException("Cannot cancel a succeeded payment intent");
                case "canceled" -> log.info("Payment Intent {} is already cancelled", paymentIntentId);
                default -> log.warn("Cannot cancel payment intent {} - current status: {}", paymentIntentId, currentStatus);
            }
        } catch (StripeException e) {
            log.error("Error cancelling payment intent ID: {}", paymentIntentId, e);
            throw new RuntimeException("Failed to cancel payment intent: " + e.getMessage(), e);
        }
    }
}

