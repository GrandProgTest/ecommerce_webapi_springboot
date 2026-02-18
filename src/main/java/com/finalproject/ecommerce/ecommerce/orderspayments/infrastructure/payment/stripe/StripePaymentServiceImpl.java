package com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe;

import com.finalproject.ecommerce.ecommerce.orderspayments.application.dtos.PaymentSessionDto;
import com.finalproject.ecommerce.ecommerce.orderspayments.application.ports.out.PaymentProvider;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.products.interfaces.acl.ProductContextFacade;
import com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration.properties.StripeProperties;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    public PaymentSessionDto initiatePayment(Order order) {
        Stripe.apiKey = stripeProperties.getApi().getSecretKey();

        try {
            List<Long> productIds = order.getItems().stream()
                    .map(orderItem -> orderItem.getProductId())
                    .collect(Collectors.toList());

            Map<Long, String> productNames = productContextFacade.getProductNames(productIds);

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
                                .setCurrency(stripeProperties.getCurrency())
                                .setUnitAmount(unitAmountInCents)
                                .setProductData(productData)
                                .build();

                SessionCreateParams.LineItem lineItem =
                        SessionCreateParams.LineItem.builder()
                                .setQuantity((long) orderItem.getQuantity())
                                .setPriceData(priceData)
                                .build();

                lineItems.add(lineItem);
            });

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(stripeProperties.getSuccess().getUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(stripeProperties.getCancel().getUrl())
                    .addAllLineItem(lineItems)
                    .putMetadata("order_id", order.getId().toString())
                    .putMetadata("user_id", order.getUserId().toString())
                    .setClientReferenceId(order.getId().toString())
                    .setExpiresAt(System.currentTimeMillis() / 1000 + 1800)
                    .build();

            Session session = Session.create(params);

            return new PaymentSessionDto(
                    session.getId(),
                    session.getUrl(),
                    "SUCCESS",
                    "Payment session created"
            );

        } catch (StripeException e) {
            log.error("Stripe error during checkout: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating Stripe checkout session: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelPayment(String sessionId) {
        Stripe.apiKey = stripeProperties.getApi().getSecretKey();

        try {
            Session session = Session.retrieve(sessionId);
            String currentStatus = session.getStatus();

            switch (currentStatus) {
                case "open" -> session.expire();
                case "complete" -> throw new IllegalStateException("Cannot cancel a completed payment session");
                case "expired" -> log.info("Stripe session {} is already expired", sessionId);
                default -> log.warn("Cannot cancel session {} - current status: {}", sessionId, currentStatus);
            }
        } catch (StripeException e) {
            log.error("Error cancelling payment session ID: {}", sessionId, e);
            throw new RuntimeException("Failed to cancel payment session: " + e.getMessage(), e);
        }
    }
}

