package com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.CartContextFacade;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.dto.CartDto;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.dto.CartItemDto;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.notifications.interfaces.acl.NotificationContextFacade;
import com.finalproject.ecommerce.ecommerce.orderspayments.application.ports.out.PaymentProvider;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.exceptions.InvalidOrderOperationException;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.exceptions.OrderNotFoundException;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.*;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.DeliveryStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.OrderStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.PaymentIntentStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.payment.stripe.dto.PaymentIntentResponse;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories.*;
import com.finalproject.ecommerce.ecommerce.products.interfaces.acl.ProductContextFacade;
import com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration.properties.StripeProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCommandServiceImpl")
class OrderCommandServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderStatusRepository orderStatusRepository;
    @Mock private DeliveryStatusRepository deliveryStatusRepository;
    @Mock private DiscountRepository discountRepository;
    @Mock private PaymentIntentRepository paymentIntentRepository;
    @Mock private PaymentIntentStatusRepository paymentIntentStatusRepository;
    @Mock private CartContextFacade cartContextFacade;
    @Mock private ProductContextFacade productContextFacade;
    @Mock private IamContextFacade iamContextFacade;
    @Mock private PaymentProvider paymentProvider;
    @Mock private NotificationContextFacade notificationContextFacade;
    @Mock private StripeProperties stripeProperties;

    @InjectMocks private OrderCommandServiceImpl service;

    private OrderStatus pendingStatus;
    private OrderStatus paidStatus;
    private OrderStatus cancelledStatus;

    @BeforeEach
    void setUp() {
        pendingStatus = new OrderStatus(OrderStatuses.PENDING, "Pending");
        paidStatus = new OrderStatus(OrderStatuses.PAID, "Paid");
        cancelledStatus = new OrderStatus(OrderStatuses.CANCELLED, "Cancelled");
    }

    private Order createOrderWithAuditFields(Long userId, Long cartId, Long addressId, OrderStatus status) {
        Order order = new Order(userId, cartId, addressId, status);
        ReflectionTestUtils.setField(order, "createdAt", Instant.now());
        ReflectionTestUtils.setField(order, "updatedAt", Instant.now());
        return order;
    }

    @Nested
    @DisplayName("Create Order From Cart")
    class CreateOrderFromCartTests {

        private CartDto activeCartDto;

        @BeforeEach
        void init() {
            activeCartDto = new CartDto(10L, 1L, true, List.of(
                    new CartItemDto(1L, 100L, 2),
                    new CartItemDto(2L, 200L, 1)
            ));
        }

        @Test
        @DisplayName("should create order successfully")
        void shouldCreateOrder() {
            var cmd = new CreateOrderFromCartCommand(1L, 10L, 100L, null);
            when(iamContextFacade.userExists(1L)).thenReturn(true);
            when(cartContextFacade.getCartById(10L)).thenReturn(Optional.of(activeCartDto));
            when(orderRepository.findByCartId(10L)).thenReturn(Optional.empty());
            when(orderStatusRepository.findByName(OrderStatuses.PENDING.name())).thenReturn(Optional.of(pendingStatus));
            when(productContextFacade.isProductDeleted(anyLong())).thenReturn(false);
            when(productContextFacade.isProductActive(anyLong())).thenReturn(true);
            when(productContextFacade.getProductPrice(100L)).thenReturn(new BigDecimal("25.00"));
            when(productContextFacade.getProductPrice(200L)).thenReturn(new BigDecimal("15.00"));
            when(productContextFacade.getProductStock(anyLong())).thenReturn(10);
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
            when(paymentProvider.initiatePayment(any())).thenReturn(
                    new PaymentIntentResponse("pi_123", "secret_456", "requires_payment_method", "ok"));
            var piStatus = new PaymentIntentStatus(PaymentIntentStatuses.REQUIRES_PAYMENT_METHOD, "desc");
            when(paymentIntentStatusRepository.findByName(PaymentIntentStatuses.REQUIRES_PAYMENT_METHOD))
                    .thenReturn(Optional.of(piStatus));

            Order result = service.handle(cmd);

            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getItems()).hasSize(2);
            verify(productContextFacade).decreaseProductStock(100L, 2);
            verify(productContextFacade).decreaseProductStock(200L, 1);
            verify(cartContextFacade).checkoutCart(1L, 10L);
        }

        @Test
        @DisplayName("should throw when user does not exist")
        void shouldThrowWhenUserNotExists() {
            var cmd = new CreateOrderFromCartCommand(999L, 10L, 100L, null);
            when(iamContextFacade.userExists(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidOrderOperationException.class)
                    .hasMessageContaining("does not exist");
        }

        @Test
        @DisplayName("should throw when cart not found")
        void shouldThrowWhenCartNotFound() {
            var cmd = new CreateOrderFromCartCommand(1L, 99L, 100L, null);
            when(iamContextFacade.userExists(1L)).thenReturn(true);
            when(cartContextFacade.getCartById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidOrderOperationException.class)
                    .hasMessageContaining("Cart with ID");
        }

        @Test
        @DisplayName("should throw when cart does not belong to user")
        void shouldThrowWhenCartNotBelongToUser() {
            var otherUserCart = new CartDto(10L, 999L, true, List.of(new CartItemDto(1L, 100L, 1)));
            var cmd = new CreateOrderFromCartCommand(1L, 10L, 100L, null);
            when(iamContextFacade.userExists(1L)).thenReturn(true);
            when(cartContextFacade.getCartById(10L)).thenReturn(Optional.of(otherUserCart));

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidOrderOperationException.class)
                    .hasMessageContaining("does not belong");
        }

        @Test
        @DisplayName("should throw when cart is empty")
        void shouldThrowWhenCartEmpty() {
            var emptyCart = new CartDto(10L, 1L, true, List.of());
            var cmd = new CreateOrderFromCartCommand(1L, 10L, 100L, null);
            when(iamContextFacade.userExists(1L)).thenReturn(true);
            when(cartContextFacade.getCartById(10L)).thenReturn(Optional.of(emptyCart));

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidOrderOperationException.class)
                    .hasMessageContaining("empty cart");
        }

        @Test
        @DisplayName("should throw when cart is not active")
        void shouldThrowWhenCartNotActive() {
            var inactiveCart = new CartDto(10L, 1L, false, List.of(new CartItemDto(1L, 100L, 1)));
            var cmd = new CreateOrderFromCartCommand(1L, 10L, 100L, null);
            when(iamContextFacade.userExists(1L)).thenReturn(true);
            when(cartContextFacade.getCartById(10L)).thenReturn(Optional.of(inactiveCart));

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidOrderOperationException.class)
                    .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("should throw when order already exists for cart")
        void shouldThrowWhenOrderAlreadyExists() {
            var cmd = new CreateOrderFromCartCommand(1L, 10L, 100L, null);
            when(iamContextFacade.userExists(1L)).thenReturn(true);
            when(cartContextFacade.getCartById(10L)).thenReturn(Optional.of(activeCartDto));
            when(orderRepository.findByCartId(10L)).thenReturn(Optional.of(new Order()));

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidOrderOperationException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("should throw when product is deleted during order creation")
        void shouldThrowWhenProductDeleted() {
            var cmd = new CreateOrderFromCartCommand(1L, 10L, 100L, null);
            when(iamContextFacade.userExists(1L)).thenReturn(true);
            when(cartContextFacade.getCartById(10L)).thenReturn(Optional.of(activeCartDto));
            when(orderRepository.findByCartId(10L)).thenReturn(Optional.empty());
            when(orderStatusRepository.findByName(OrderStatuses.PENDING.name())).thenReturn(Optional.of(pendingStatus));
            when(productContextFacade.isProductDeleted(100L)).thenReturn(true);
            when(productContextFacade.getProductName(100L)).thenReturn("Deleted Product");

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidOrderOperationException.class)
                    .hasMessageContaining("not available");
        }
    }

    @Nested
    @DisplayName("Cancel Order")
    class CancelOrderTests {

        @Test
        @DisplayName("should cancel pending order and restore stock")
        void shouldCancelAndRestoreStock() {
            Order order = createOrderWithAuditFields(1L, 10L, 100L, pendingStatus);
            order.addItem(200L, new BigDecimal("25.00"), 2);
            order.setStripePaymentInfo("pi_123", "secret");
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findByName(OrderStatuses.CANCELLED.name())).thenReturn(Optional.of(cancelledStatus));
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
            when(iamContextFacade.getUserEmail(1L)).thenReturn("user@test.com");
            when(iamContextFacade.getUsernameById(1L)).thenReturn("testuser");

            Order result = service.handle(new CancelOrderCommand(1L));

            assertThat(result.isCancelled()).isTrue();
            verify(productContextFacade).increaseProductStock(200L, 2);
            verify(paymentProvider).cancelPayment("pi_123");
        }

        @Test
        @DisplayName("should throw when order not found")
        void shouldThrowWhenNotFound() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new CancelOrderCommand(99L)))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("should not fail when payment cancellation fails")
        void shouldNotFailWhenPaymentCancelFails() {
            Order order = createOrderWithAuditFields(1L, 10L, 100L, pendingStatus);
            order.setStripePaymentInfo("pi_123", "secret");
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findByName(OrderStatuses.CANCELLED.name())).thenReturn(Optional.of(cancelledStatus));
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
            doThrow(new RuntimeException("Stripe error")).when(paymentProvider).cancelPayment("pi_123");
            when(iamContextFacade.getUserEmail(1L)).thenReturn("user@test.com");
            when(iamContextFacade.getUsernameById(1L)).thenReturn("testuser");

            Order result = service.handle(new CancelOrderCommand(1L));

            assertThat(result.isCancelled()).isTrue();
        }

        @Test
        @DisplayName("should skip payment cancellation when no stripe payment intent")
        void shouldSkipPaymentCancelWhenNoStripeId() {
            Order order = createOrderWithAuditFields(1L, 10L, 100L, pendingStatus);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(orderStatusRepository.findByName(OrderStatuses.CANCELLED.name())).thenReturn(Optional.of(cancelledStatus));
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
            when(iamContextFacade.getUserEmail(1L)).thenReturn("user@test.com");
            when(iamContextFacade.getUsernameById(1L)).thenReturn("testuser");

            service.handle(new CancelOrderCommand(1L));

            verify(paymentProvider, never()).cancelPayment(anyString());
        }
    }


    @Nested
    @DisplayName("Mark Order As Paid")
    class MarkOrderAsPaidTests {

        @Test
        @DisplayName("should mark order as paid via stripe webhook")
        void shouldMarkAsPaid() {
            Order order = createOrderWithAuditFields(1L, 10L, 100L, pendingStatus);
            when(orderRepository.findByStripePaymentIntentId("pi_123")).thenReturn(Optional.of(order));
            when(orderStatusRepository.findByName(OrderStatuses.PAID.name())).thenReturn(Optional.of(paidStatus));
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
            when(iamContextFacade.getUserEmail(1L)).thenReturn("user@test.com");
            when(iamContextFacade.getUsernameById(1L)).thenReturn("testuser");

            Order result = service.handle(new MarkOrderAsPaidCommand("pi_123"));

            assertThat(result.isPaid()).isTrue();
            verify(notificationContextFacade).sendOrderStatusUpdate(
                    eq("user@test.com"), eq("testuser"), any(), any(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("should be idempotent for already paid orders")
        void shouldBeIdempotent() {
            Order order = createOrderWithAuditFields(1L, 10L, 100L, pendingStatus);
            order.markAsPaid(paidStatus);
            when(orderRepository.findByStripePaymentIntentId("pi_123")).thenReturn(Optional.of(order));

            Order result = service.handle(new MarkOrderAsPaidCommand("pi_123"));

            assertThat(result.isPaid()).isTrue();
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when order not found by stripe id")
        void shouldThrowWhenNotFound() {
            when(orderRepository.findByStripePaymentIntentId("pi_999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new MarkOrderAsPaidCommand("pi_999")))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }


    @Nested
    @DisplayName("Update Delivery Status")
    class UpdateDeliveryStatusTests {

        @Test
        @DisplayName("should update delivery status for paid order")
        void shouldUpdateDeliveryStatus() {
            Order order = createOrderWithAuditFields(1L, 10L, 100L, pendingStatus);
            order.markAsPaid(paidStatus);
            DeliveryStatus packedStatus = new DeliveryStatus(DeliveryStatuses.PACKED, "Packed");
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(deliveryStatusRepository.findByName(DeliveryStatuses.PACKED.name())).thenReturn(Optional.of(packedStatus));
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
            when(iamContextFacade.getUserEmail(1L)).thenReturn("user@test.com");
            when(iamContextFacade.getUsernameById(1L)).thenReturn("testuser");

            Order result = service.handle(new UpdateOrderDeliveryStatusCommand(1L, DeliveryStatuses.PACKED));

            assertThat(result.getDeliveryStatus()).isEqualTo(packedStatus);
            verify(notificationContextFacade).sendOrderStatusUpdate(
                    eq("user@test.com"), eq("testuser"), any(), any(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("should throw when order not found")
        void shouldThrowWhenNotFound() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new UpdateOrderDeliveryStatusCommand(99L, DeliveryStatuses.SHIPPED)))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when order is not paid")
        void shouldThrowWhenNotPaid() {
            Order order = createOrderWithAuditFields(1L, 10L, 100L, pendingStatus);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> service.handle(new UpdateOrderDeliveryStatusCommand(1L, DeliveryStatuses.SHIPPED)))
                    .isInstanceOf(InvalidOrderOperationException.class)
                    .hasMessageContaining("must be PAID");
        }

        @Test
        @DisplayName("should throw when delivery status not found in DB")
        void shouldThrowWhenStatusNotFound() {
            Order order = createOrderWithAuditFields(1L, 10L, 100L, pendingStatus);
            order.markAsPaid(paidStatus);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(deliveryStatusRepository.findByName(DeliveryStatuses.SHIPPED.name())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new UpdateOrderDeliveryStatusCommand(1L, DeliveryStatuses.SHIPPED)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not found in database");
        }
    }


    @Nested
    @DisplayName("Seed Order Statuses")
    class SeedOrderStatusTests {

        @Test
        @DisplayName("should seed missing order statuses")
        void shouldSeedMissing() {
            when(orderStatusRepository.existsByName(anyString())).thenReturn(false);

            service.handle(new SeedOrderStatusCommand());

            verify(orderStatusRepository, times(OrderStatuses.values().length)).save(any(OrderStatus.class));
        }

        @Test
        @DisplayName("should not seed existing order statuses")
        void shouldNotSeedExisting() {
            when(orderStatusRepository.existsByName(anyString())).thenReturn(true);

            service.handle(new SeedOrderStatusCommand());

            verify(orderStatusRepository, never()).save(any(OrderStatus.class));
        }
    }


    @Nested
    @DisplayName("Seed Delivery Statuses")
    class SeedDeliveryStatusTests {

        @Test
        @DisplayName("should seed missing delivery statuses")
        void shouldSeedMissing() {
            when(deliveryStatusRepository.findByName(anyString())).thenReturn(Optional.empty());

            service.handle(new SeedDeliveryStatusCommand());

            verify(deliveryStatusRepository, times(DeliveryStatuses.values().length)).save(any(DeliveryStatus.class));
        }

        @Test
        @DisplayName("should not seed existing delivery statuses")
        void shouldNotSeedExisting() {
            when(deliveryStatusRepository.findByName(anyString()))
                    .thenReturn(Optional.of(new DeliveryStatus(DeliveryStatuses.PACKED, "desc")));

            service.handle(new SeedDeliveryStatusCommand());

            verify(deliveryStatusRepository, never()).save(any(DeliveryStatus.class));
        }
    }


    @Nested
    @DisplayName("Seed Payment Intent Statuses")
    class SeedPaymentIntentStatusTests {

        @Test
        @DisplayName("should seed missing payment intent statuses")
        void shouldSeedMissing() {
            when(paymentIntentStatusRepository.existsByName(any(PaymentIntentStatuses.class))).thenReturn(false);

            service.handle(new SeedPaymentIntentStatusCommand());

            verify(paymentIntentStatusRepository, times(PaymentIntentStatuses.values().length))
                    .save(any(PaymentIntentStatus.class));
        }

        @Test
        @DisplayName("should not seed existing payment intent statuses")
        void shouldNotSeedExisting() {
            when(paymentIntentStatusRepository.existsByName(any(PaymentIntentStatuses.class))).thenReturn(true);

            service.handle(new SeedPaymentIntentStatusCommand());

            verify(paymentIntentStatusRepository, never()).save(any(PaymentIntentStatus.class));
        }
    }
}

