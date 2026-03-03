package com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.CartContextFacade;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.dto.CartDto;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.notifications.interfaces.acl.NotificationContextFacade;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.*;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.DeliveryStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.PaymentIntentStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe.dto.PaymentIntentResponse;
import com.finalproject.ecommerce.ecommerce.orderspayments.application.ports.out.PaymentProvider;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.exceptions.InvalidOrderOperationException;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.exceptions.OrderNotFoundException;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.OrderStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderCommandService;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories.*;
import com.finalproject.ecommerce.ecommerce.products.interfaces.acl.ProductContextFacade;
import com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration.properties.StripeProperties;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.param.PaymentIntentConfirmParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class OrderCommandServiceImpl implements OrderCommandService {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final DeliveryStatusRepository deliveryStatusRepository;
    private final DiscountRepository discountRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentIntentStatusRepository paymentIntentStatusRepository;
    private final CartContextFacade cartContextFacade;
    private final ProductContextFacade productContextFacade;
    private final IamContextFacade iamContextFacade;
    private final PaymentProvider paymentProvider;
    private final NotificationContextFacade notificationContextFacade;
    private final StripeProperties stripeProperties;


    public OrderCommandServiceImpl(OrderRepository orderRepository, OrderStatusRepository orderStatusRepository, DeliveryStatusRepository deliveryStatusRepository, DiscountRepository discountRepository, PaymentIntentRepository paymentIntentRepository, PaymentIntentStatusRepository paymentIntentStatusRepository, CartContextFacade cartContextFacade, ProductContextFacade productContextFacade, IamContextFacade iamContextFacade, PaymentProvider paymentProvider, NotificationContextFacade notificationContextFacade, StripeProperties stripeProperties) {
        this.orderRepository = orderRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.deliveryStatusRepository = deliveryStatusRepository;
        this.discountRepository = discountRepository;
        this.paymentIntentRepository = paymentIntentRepository;
        this.paymentIntentStatusRepository = paymentIntentStatusRepository;
        this.cartContextFacade = cartContextFacade;
        this.productContextFacade = productContextFacade;
        this.iamContextFacade = iamContextFacade;
        this.paymentProvider = paymentProvider;
        this.notificationContextFacade = notificationContextFacade;
        this.stripeProperties = stripeProperties;
    }

    @Override
    @Transactional
    public Order handle(CreateOrderFromCartCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        if (!iamContextFacade.userExists(command.userId())) {
            throw new InvalidOrderOperationException("User with ID " + command.userId() + " does not exist");
        }

        iamContextFacade.validateAddressBelongsToUser(command.addressId(), command.userId());

        CartDto cartDto = cartContextFacade.getCartById(command.cartId())
                .orElseThrow(() -> new InvalidOrderOperationException("Cart with ID " + command.cartId() + " not found"));

        if (!cartDto.userId().equals(command.userId())) {
            throw new InvalidOrderOperationException("Cart does not belong to the user");
        }

        if (cartDto.items().isEmpty()) {
            throw new InvalidOrderOperationException("Cannot create order from empty cart");
        }

        if (!cartDto.isActive()) {
            throw new InvalidOrderOperationException("Cart is not active");
        }

        if (orderRepository.findByCartId(command.cartId()).isPresent()) {
            throw new InvalidOrderOperationException("Order already exists for this cart");
        }

        OrderStatus pendingStatus = orderStatusRepository.findByName(OrderStatuses.PENDING.name())
                .orElseThrow(() -> new IllegalStateException("Pending order status not found"));

        Order order = new Order(command.userId(), command.cartId(), command.addressId(), pendingStatus);

        cartDto.items().forEach(cartItem -> {
            if (productContextFacade.isProductDeleted(cartItem.productId()) || !productContextFacade.isProductActive(cartItem.productId())) {
                String productName = productContextFacade.getProductName(cartItem.productId());
                throw new InvalidOrderOperationException(
                        "Product '" + productName + "' (ID: " + cartItem.productId() + ") is not available and cannot be purchased"
                );
            }


            BigDecimal price = productContextFacade.getProductPrice(cartItem.productId());
            boolean isPurchasedWithSalePrice = productContextFacade.hasActiveSalePrice(cartItem.productId());
            order.addItem(cartItem.productId(), price, cartItem.quantity(), isPurchasedWithSalePrice);

            productContextFacade.decreaseProductStock(cartItem.productId(), cartItem.quantity());

            Integer currentStock = productContextFacade.getProductStock(cartItem.productId());
            if (currentStock != null && currentStock <= 3) {
                notifyUsersOfLowStock(cartItem.productId(), currentStock);
            }
        });

        if (command.discountCode() != null && !command.discountCode().isBlank()) {
            Discount discount = discountRepository.findByCode(command.discountCode())
                    .orElseThrow(() -> new InvalidOrderOperationException("Discount code not found"));
            order.applyDiscount(discount);
        }

        Order savedOrder = orderRepository.save(order);

        PaymentIntentResponse paymentIntentDto = paymentProvider.initiatePayment(savedOrder);

        savedOrder.setStripePaymentInfo(paymentIntentDto.paymentIntentId(), paymentIntentDto.clientSecret());
        savedOrder = orderRepository.save(savedOrder);

        PaymentIntentStatuses statusEnum = mapStripeStatusToEnum(paymentIntentDto.status());

        PaymentIntentStatus paymentIntentStatus = paymentIntentStatusRepository
                .findByName(statusEnum)
                .orElseThrow(() -> new IllegalStateException(
                        "PaymentIntentStatus " + statusEnum + " not found in database"));

        PaymentIntent paymentIntent = PaymentIntent.fromStripeResponse(
                savedOrder,
                paymentIntentDto.paymentIntentId(),
                paymentIntentDto.clientSecret(),
                paymentIntentStatus,
                savedOrder.getTotalAmount(),
                "usd"
        );
        paymentIntentRepository.save(paymentIntent);

        log.info("PaymentIntent entity created for Order {} with Stripe Payment Intent {} and status {}",
                savedOrder.getId(), paymentIntent.getStripePaymentIntentId(), paymentIntentStatus.getName());

        cartContextFacade.checkoutCart(command.userId(), command.cartId());

        return savedOrder;
    }

    @Override
    @Transactional
    public Order handle(CancelOrderCommand command) {
        Order order = orderRepository.findById(command.orderId()).orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + command.orderId()));

        iamContextFacade.validateUserCanAccessResource(order.getUserId());

        OrderStatus cancelledStatus = orderStatusRepository.findByName(OrderStatuses.CANCELLED.name()).orElseThrow(() -> new IllegalStateException("Cancelled order status not found"));

        order.cancel(cancelledStatus);

        order.getItems().forEach(orderItem -> {
            productContextFacade.increaseProductStock(orderItem.getProductId(), orderItem.getQuantity());
        });

        if (order.getStripePaymentIntentId() != null && !order.getStripePaymentIntentId().isEmpty()) {
            try {
                paymentProvider.cancelPayment(order.getStripePaymentIntentId());
            } catch (RuntimeException e) {
                log.error("Failed to cancel payment intent {} for order {}: {}",
                        order.getStripePaymentIntentId(), order.getId(), e.getMessage());
            }
        }

        Order savedOrder = orderRepository.save(order);

        sendOrderStatusNotification(savedOrder, "Your order has been cancelled. If you were charged, a refund will be processed.");

        return savedOrder;
    }

    @Override
    @Transactional
    public Order handle(MarkOrderAsPaidCommand command) {
        Order order = orderRepository.findByStripePaymentIntentId(command.stripePaymentIntentId())
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found with Stripe Payment Intent ID: " + command.stripePaymentIntentId()));

        if (order.isPaid()) {
            log.warn("Idempotent webhook: Order {} is already PAID. Stripe Payment Intent {} - ignoring duplicate event",
                    order.getId(), command.stripePaymentIntentId());
            return order;
        }

        OrderStatus paidStatus = orderStatusRepository.findByName(OrderStatuses.PAID.name())
                .orElseThrow(() -> new IllegalStateException("Paid order status not found"));

        order.markAsPaid(paidStatus);

        log.info("Order {} marked as paid via Stripe Payment Intent {}", order.getId(), command.stripePaymentIntentId());

        Order savedOrder = orderRepository.save(order);

        sendOrderStatusNotification(savedOrder, "Your payment has been successfully processed!");

        return savedOrder;
    }

    @Override
    @Transactional
    public Order handle(ConfirmPaymentCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + command.orderId()));

        iamContextFacade.validateUserCanAccessResource(order.getUserId());

        if (order.isPaid()) {
            throw new InvalidOrderOperationException("Order " + command.orderId() + " is already paid");
        }

        if (order.getStripePaymentIntentId() == null) {
            throw new InvalidOrderOperationException("Order " + command.orderId() + " does not have a payment intent");
        }

        try {
            Stripe.apiKey = stripeProperties.getApi().getSecretKey();

            // The payment intent here is being called directly
            // from the stripe library so that it does not messes up
            // with the domain payment intent we created
            com.stripe.model.PaymentIntent stripePI =
                    com.stripe.model.PaymentIntent.retrieve(order.getStripePaymentIntentId());

            PaymentIntentConfirmParams params =
                    PaymentIntentConfirmParams.builder()
                            .setPaymentMethod(command.paymentMethodId())
                            .setReturnUrl("https://example.com/order-confirmation")
                            .build();

            stripePI = stripePI.confirm(params);

            log.info("Payment Intent {} confirmed with status: {}",
                    stripePI.getId(), stripePI.getStatus());

            PaymentIntent paymentIntent = paymentIntentRepository
                    .findByStripePaymentIntentId(order.getStripePaymentIntentId())
                    .orElseThrow(() -> new RuntimeException("PaymentIntent entity not found"));

            PaymentIntentStatuses statusEnum = mapStripeStatusToEnum(stripePI.getStatus());
            PaymentIntentStatus newStatus = paymentIntentStatusRepository
                    .findByName(statusEnum)
                    .orElse(paymentIntent.getStatus());

            paymentIntent.updateStatus(newStatus);
            paymentIntentRepository.save(paymentIntent);

            if ("succeeded".equals(stripePI.getStatus())) {
                OrderStatus paidStatus = orderStatusRepository.findByName(OrderStatuses.PAID.name())
                        .orElseThrow(() -> new IllegalStateException("Paid order status not found"));

                order.markAsPaid(paidStatus);
                order = orderRepository.save(order);

                sendOrderStatusNotification(order, "Your payment has been successfully processed!");

                log.info("Order {} marked as PAID after direct payment confirmation", order.getId());
            }

            return order;

        } catch (StripeException e) {
            log.error("Error confirming payment for order {}: {}", command.orderId(), e.getMessage(), e);
            throw new InvalidOrderOperationException("Payment confirmation failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handle(SeedOrderStatusCommand command) {
        Arrays.stream(OrderStatuses.values()).forEach(status -> {
            if (!orderStatusRepository.existsByName(status.name())) {
                String description = switch (status) {
                    case PENDING -> "Order is pending payment";
                    case PAID -> "Order has been paid";
                    case CANCELLED -> "Order has been cancelled";
                };
                orderStatusRepository.save(new OrderStatus(status, description));
            }
        });
    }

    @Override
    @Transactional
    public void handle(SeedDeliveryStatusCommand command) {
        Arrays.stream(DeliveryStatuses.values()).forEach(status -> {
            if (!deliveryStatusRepository.findByName(status.name()).isPresent()) {
                String description = switch (status) {
                    case PACKED -> "Order has been packed and is ready for shipping";
                    case SHIPPED -> "Order has been shipped";
                    case IN_TRANSIT -> "Order is in transit";
                    case DELIVERED -> "Order has been delivered";
                };
                deliveryStatusRepository.save(new DeliveryStatus(status, description));
            }
        });
    }

    @Override
    @Transactional
    public void handle(SeedPaymentIntentStatusCommand command) {
        Arrays.stream(PaymentIntentStatuses.values()).forEach(status -> {
            if (!paymentIntentStatusRepository.existsByName(status)) {
                String description = switch (status) {
                    case REQUIRES_PAYMENT_METHOD -> "Payment intent requires a payment method";
                    case REQUIRES_CONFIRMATION -> "Payment intent requires confirmation";
                    case REQUIRES_ACTION -> "Payment intent requires additional action (e.g., 3D Secure)";
                    case PROCESSING -> "Payment is being processed";
                    case REQUIRES_CAPTURE -> "Payment requires manual capture";
                    case CANCELED -> "Payment intent was canceled";
                    case SUCCEEDED -> "Payment succeeded";
                };
                paymentIntentStatusRepository.save(new PaymentIntentStatus(status, description));
            }
        });
    }

    @Override
    public Order handle(UpdateOrderDeliveryStatusCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + command.orderId()));

        if (!order.isPaid()) {
            throw new InvalidOrderOperationException(
                    "Cannot update delivery status. Order must be PAID first. Current status: " +
                            order.getStatus().getName());
        }

        DeliveryStatus newDeliveryStatus = deliveryStatusRepository.findByName(command.newDeliveryStatus().name())
                .orElseThrow(() -> new IllegalStateException(
                        "Delivery status " + command.newDeliveryStatus().name() + " not found in database"));

        order.updateDeliveryStatus(newDeliveryStatus);

        log.info("Order {} delivery status updated to {} by manager", order.getId(), command.newDeliveryStatus().name());

        Order savedOrder = orderRepository.save(order);

        String message = switch (command.newDeliveryStatus()) {
            case PACKED -> "Your order has been packed and is ready for shipping.";
            case SHIPPED -> "Your order has been shipped! It's on its way to you.";
            case IN_TRANSIT -> "Your order is in transit and will arrive soon.";
            case DELIVERED -> "Your order has been delivered! We hope you enjoy your purchase.";
        };

        sendDeliveryStatusNotification(savedOrder, command.newDeliveryStatus().name(), message);

        return savedOrder;
    }

    private void notifyUsersOfLowStock(Long productId, Integer currentStock) {
        try {
            List<Long> userIds = productContextFacade.getUsersWhoLikedProduct(productId);
            if (userIds.isEmpty()) {
                log.debug("No users have liked product {}, skipping notifications", productId);
                return;
            }

            String productName = productContextFacade.getProductName(productId);

            Map<Long, String> userEmails = iamContextFacade.getUserEmails(userIds);

            if (userEmails.isEmpty()) {
                log.warn("No user emails found for product {} notification", productId);
                return;
            }

            Set<String> recipientEmails = new HashSet<>(userEmails.values());

            notificationContextFacade.sendLowStockAlertBatch(
                    recipientEmails,
                    productName,
                    currentStock
            );

            log.info("Queued async low stock notification (BCC) for product {} to {} unique users",
                    productId, recipientEmails.size());

        } catch (Exception e) {
            log.error("Error queueing low stock notifications for product {}: {}",
                    productId, e.getMessage());
        }
    }

    private void sendOrderStatusNotification(Order order, String statusMessage) {
        try {
            String userEmail = iamContextFacade.getUserEmail(order.getUserId());
            if (userEmail == null || userEmail.isBlank()) {
                log.warn("No email found for user {} - skipping order status notification for order {}",
                        order.getUserId(), order.getId());
                return;
            }

            String username = iamContextFacade.getUsernameById(order.getUserId());

            String totalAmount = String.format("%.2f", order.getTotalAmount());
            String orderDate = order.getCreatedAt().toString();
            String orderStatus = order.getStatus().getName();

            notificationContextFacade.sendOrderStatusUpdate(
                    userEmail,
                    username != null ? username : "Customer",
                    order.getId(),
                    orderStatus,
                    statusMessage,
                    totalAmount,
                    orderDate
            );

            log.info("Order status notification sent for order {} to user {}", order.getId(), order.getUserId());

        } catch (Exception e) {
            log.error("Failed to send order status notification for order {}: {}",
                    order.getId(), e.getMessage());
        }
    }

    private void sendDeliveryStatusNotification(Order order, String deliveryStatus, String statusMessage) {
        try {
            String userEmail = iamContextFacade.getUserEmail(order.getUserId());
            if (userEmail == null || userEmail.isBlank()) {
                log.warn("No email found for user {} - skipping delivery status notification for order {}",
                        order.getUserId(), order.getId());
                return;
            }

            String username = iamContextFacade.getUsernameById(order.getUserId());

            String totalAmount = String.format("%.2f", order.getTotalAmount());
            String orderDate = order.getCreatedAt().toString();

            notificationContextFacade.sendOrderStatusUpdate(
                    userEmail,
                    username != null ? username : "Customer",
                    order.getId(),
                    deliveryStatus,
                    statusMessage,
                    totalAmount,
                    orderDate
            );

            log.info("Delivery status notification sent for order {} to user {} with status {}",
                    order.getId(), order.getUserId(), deliveryStatus);

        } catch (Exception e) {
            log.error("Failed to send delivery status notification for order {}: {}",
                    order.getId(), e.getMessage());
        }
    }

    private PaymentIntentStatuses mapStripeStatusToEnum(String stripeStatus) {
        if (stripeStatus == null) {
            return PaymentIntentStatuses.REQUIRES_PAYMENT_METHOD;
        }

        String normalized = stripeStatus.toUpperCase().replace(".", "_");

        try {
            return PaymentIntentStatuses.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return PaymentIntentStatuses.REQUIRES_PAYMENT_METHOD;
        }
    }
}
