package com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe.webhook;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.MarkOrderAsPaidCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.PaymentIntent;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.PaymentIntentStatus;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.StripeWebhookEvent;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.PaymentIntentStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderCommandService;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories.PaymentIntentRepository;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories.PaymentIntentStatusRepository;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories.StripeWebhookEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class StripeWebhookEventHandler {

    private final OrderCommandService orderCommandService;
    private final StripeWebhookEventRepository webhookEventRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentIntentStatusRepository paymentIntentStatusRepository;

    public StripeWebhookEventHandler(OrderCommandService orderCommandService,
                                     StripeWebhookEventRepository webhookEventRepository,
                                     PaymentIntentRepository paymentIntentRepository,
                                     PaymentIntentStatusRepository paymentIntentStatusRepository) {
        this.orderCommandService = orderCommandService;
        this.webhookEventRepository = webhookEventRepository;
        this.paymentIntentRepository = paymentIntentRepository;
        this.paymentIntentStatusRepository = paymentIntentStatusRepository;
    }

    @Transactional
    public void handlePaymentIntentSucceeded(String eventId, String paymentIntentId) {
        log.info("Processing payment_intent.succeeded event {} for Payment Intent: {}", eventId, paymentIntentId);

        if (webhookEventRepository.existsByStripeEventId(eventId)) {
            log.warn("Idempotent webhook: Event {} already processed. Ignoring duplicate.", eventId);
            return;
        }

        try {
            PaymentIntentStatus succeededStatus = paymentIntentStatusRepository
                    .findByName(PaymentIntentStatuses.SUCCEEDED)
                    .orElseThrow(() -> new IllegalStateException("SUCCEEDED payment intent status not found in database"));

            PaymentIntent paymentIntent = paymentIntentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("PaymentIntent not found for Payment Intent ID: " + paymentIntentId));

            paymentIntent.updateStatus(succeededStatus);
            paymentIntentRepository.save(paymentIntent);
            log.info("PaymentIntent entity updated to SUCCEEDED for Payment Intent {}", paymentIntentId);

            MarkOrderAsPaidCommand command = new MarkOrderAsPaidCommand(paymentIntentId);
            var paidOrder = orderCommandService.handle(command);

            StripeWebhookEvent webhookEvent = new StripeWebhookEvent(
                    eventId,
                    "payment_intent.succeeded",
                    paymentIntentId,
                    paidOrder.getId()
            );
            webhookEventRepository.save(webhookEvent);

            log.info("Order {} marked as PAID successfully via Stripe Payment Intent {}. Event {} recorded.",
                    paidOrder.getId(), paymentIntentId, eventId);
        } catch (Exception e) {
            log.error("Failed to mark order as paid for Stripe Payment Intent {}, event {}: {}",
                    paymentIntentId, eventId, e.getMessage(), e);

            StripeWebhookEvent failedEvent = new StripeWebhookEvent(eventId, "payment_intent.succeeded", paymentIntentId, null);
            failedEvent.markAsFailed(e.getMessage());
            webhookEventRepository.save(failedEvent);

            throw new RuntimeException("Failed to process payment completion: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void handlePaymentIntentFailed(String eventId, String paymentIntentId) {
        log.warn("Payment failed for Stripe Payment Intent: {}, event: {}", paymentIntentId, eventId);

        if (webhookEventRepository.existsByStripeEventId(eventId)) {
            log.warn("Idempotent webhook: Event {} already processed. Ignoring duplicate.", eventId);
            return;
        }

        try {
            PaymentIntentStatus canceledStatus = paymentIntentStatusRepository
                    .findByName(PaymentIntentStatuses.CANCELED)
                    .orElseThrow(() -> new IllegalStateException("CANCELED payment intent status not found in database"));

            PaymentIntent paymentIntent = paymentIntentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("PaymentIntent not found for Payment Intent ID: " + paymentIntentId));

            paymentIntent.markAsFailed(canceledStatus, "Payment failed");
            paymentIntentRepository.save(paymentIntent);
            log.info("PaymentIntent entity marked as failed for Payment Intent {}", paymentIntentId);
        } catch (Exception e) {
            log.error("Error updating payment intent status for Payment Intent {}: {}", paymentIntentId, e.getMessage());
        }

        StripeWebhookEvent webhookEvent = new StripeWebhookEvent(eventId, "payment_intent.payment_failed", paymentIntentId, null);
        webhookEvent.markAsFailed("Payment failed");
        webhookEventRepository.save(webhookEvent);
    }

    @Transactional
    public void handlePaymentIntentCanceled(String eventId, String paymentIntentId) {
        log.info("Payment Intent canceled: {}, event: {}", paymentIntentId, eventId);

        if (webhookEventRepository.existsByStripeEventId(eventId)) {
            log.warn("Idempotent webhook: Event {} already processed. Ignoring duplicate.", eventId);
            return;
        }

        try {
            PaymentIntentStatus canceledStatus = paymentIntentStatusRepository
                    .findByName(PaymentIntentStatuses.CANCELED)
                    .orElseThrow(() -> new IllegalStateException("CANCELED payment intent status not found in database"));

            PaymentIntent paymentIntent = paymentIntentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("PaymentIntent not found for Payment Intent ID: " + paymentIntentId));

            paymentIntent.updateStatus(canceledStatus);
            paymentIntentRepository.save(paymentIntent);
            log.info("PaymentIntent entity marked as CANCELED for Payment Intent {}", paymentIntentId);
        } catch (Exception e) {
            log.error("Error updating payment intent status for Payment Intent {}: {}", paymentIntentId, e.getMessage());
        }

        StripeWebhookEvent webhookEvent = new StripeWebhookEvent(eventId, "payment_intent.canceled", paymentIntentId, null);
        webhookEventRepository.save(webhookEvent);
    }
}

