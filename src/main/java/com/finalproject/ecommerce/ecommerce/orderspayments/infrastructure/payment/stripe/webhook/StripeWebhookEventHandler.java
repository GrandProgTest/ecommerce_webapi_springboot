package com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe.webhook;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.MarkOrderAsPaidCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.StripeWebhookEvent;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderCommandService;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories.StripeWebhookEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class StripeWebhookEventHandler {

    private final OrderCommandService orderCommandService;
    private final StripeWebhookEventRepository webhookEventRepository;

    public StripeWebhookEventHandler(OrderCommandService orderCommandService,
                                     StripeWebhookEventRepository webhookEventRepository) {
        this.orderCommandService = orderCommandService;
        this.webhookEventRepository = webhookEventRepository;
    }

    @Transactional
    public void handleCheckoutSessionCompleted(String eventId, String sessionId) {
        log.info("Processing checkout.session.completed event {} for session: {}", eventId, sessionId);

        if (webhookEventRepository.existsByStripeEventId(eventId)) {
            log.warn("Idempotent webhook: Event {} already processed. Ignoring duplicate.", eventId);
            return;
        }

        try {
            MarkOrderAsPaidCommand command = new MarkOrderAsPaidCommand(sessionId);
            var paidOrder = orderCommandService.handle(command);

            StripeWebhookEvent webhookEvent = new StripeWebhookEvent(
                    eventId,
                    "checkout.session.completed",
                    sessionId,
                    paidOrder.getId()
            );
            webhookEventRepository.save(webhookEvent);

            log.info("Order {} marked as PAID successfully via Stripe session {}. Event {} recorded.",
                    paidOrder.getId(), sessionId, eventId);
        } catch (Exception e) {
            log.error("Failed to mark order as paid for Stripe session {}, event {}: {}",
                    sessionId, eventId, e.getMessage(), e);

            StripeWebhookEvent failedEvent = new StripeWebhookEvent(eventId, "checkout.session.completed", sessionId, null);
            failedEvent.markAsFailed(e.getMessage());
            webhookEventRepository.save(failedEvent);

            throw new RuntimeException("Failed to process payment completion: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void handleAsyncPaymentSucceeded(String eventId, String sessionId) {
        log.info("Processing checkout.session.async_payment_succeeded event {} for session: {}", eventId, sessionId);
        handleCheckoutSessionCompleted(eventId, sessionId);
    }

    @Transactional
    public void handleAsyncPaymentFailed(String eventId, String sessionId) {
        log.warn("Async payment failed for Stripe session: {}, event: {}", sessionId, eventId);

        if (webhookEventRepository.existsByStripeEventId(eventId)) {
            log.warn("Idempotent webhook: Event {} already processed. Ignoring duplicate.", eventId);
            return;
        }

        StripeWebhookEvent webhookEvent = new StripeWebhookEvent(eventId, "checkout.session.async_payment_failed", sessionId, null);
        webhookEvent.markAsFailed("Async payment failed");
        webhookEventRepository.save(webhookEvent);
    }
}

