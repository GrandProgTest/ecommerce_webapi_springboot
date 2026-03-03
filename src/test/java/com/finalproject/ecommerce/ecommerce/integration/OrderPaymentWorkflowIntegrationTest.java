package com.finalproject.ecommerce.ecommerce.integration;

import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.CartContextFacade;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.dto.CartDto;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.dto.CartItemDto;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.CreateAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Address;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.valueobjects.Roles;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.notifications.interfaces.acl.NotificationContextFacade;
import com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.commandservices.OrderCommandServiceImpl;
import com.finalproject.ecommerce.ecommerce.orderspayments.application.ports.out.PaymentProvider;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Order and Payment Workflow Integration Tests")
class OrderPaymentWorkflowIntegrationTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderStatusRepository orderStatusRepository;
    @Mock
    private DeliveryStatusRepository deliveryStatusRepository;
    @Mock
    private DiscountRepository discountRepository;
    @Mock
    private PaymentIntentRepository paymentIntentRepository;
    @Mock
    private PaymentIntentStatusRepository paymentIntentStatusRepository;
    @Mock
    private CartContextFacade cartContextFacade;
    @Mock
    private ProductContextFacade productContextFacade;
    @Mock
    private IamContextFacade iamContextFacade;
    @Mock
    private PaymentProvider paymentProvider;
    @Mock
    private NotificationContextFacade notificationContextFacade;
    @Mock
    private StripeProperties stripeProperties;

    private OrderCommandServiceImpl orderCommandService;

    private User testUser;
    private Address testAddress;
    private OrderStatus pendingStatus;
    private OrderStatus paidStatus;
    private OrderStatus cancelledStatus;
    private DeliveryStatus packedStatus;
    private DeliveryStatus shippedStatus;
    private DeliveryStatus inTransitStatus;
    private DeliveryStatus deliveredStatus;
    private PaymentIntentStatus paymentIntentSucceededStatus;

    @BeforeEach
    void setUp() {
        orderCommandService = new OrderCommandServiceImpl(
                orderRepository,
                orderStatusRepository,
                deliveryStatusRepository,
                discountRepository,
                paymentIntentRepository,
                paymentIntentStatusRepository,
                cartContextFacade,
                productContextFacade,
                iamContextFacade,
                paymentProvider,
                notificationContextFacade,
                stripeProperties
        );

        Role clientRole = new Role(Roles.ROLE_CLIENT);
        testUser = new User("client1", "client1@example.com", "hashedPass123", clientRole);
        setId(testUser, 1L);
        testUser.activate();

        CreateAddressCommand addressCmd = new CreateAddressCommand(
                "123 Main St",
                "New York",
                "NY",
                "USA",
                "10001",
                true
        );
        testAddress = new Address(addressCmd, testUser);
        setId(testAddress, 1L);

        pendingStatus = new OrderStatus(OrderStatuses.PENDING, "Order is pending payment");
        paidStatus = new OrderStatus(OrderStatuses.PAID, "Order has been paid");
        cancelledStatus = new OrderStatus(OrderStatuses.CANCELLED, "Order has been cancelled");

        packedStatus = new DeliveryStatus(DeliveryStatuses.PACKED, "Order has been packed");
        shippedStatus = new DeliveryStatus(DeliveryStatuses.SHIPPED, "Order has been shipped");
        inTransitStatus = new DeliveryStatus(DeliveryStatuses.IN_TRANSIT, "Order is in transit");
        deliveredStatus = new DeliveryStatus(DeliveryStatuses.DELIVERED, "Order has been delivered");

        paymentIntentSucceededStatus = new PaymentIntentStatus(
                PaymentIntentStatuses.SUCCEEDED,
                "Payment succeeded"
        );

        StripeProperties.Api apiConfig = mock(StripeProperties.Api.class);
        when(stripeProperties.getApi()).thenReturn(apiConfig);
        when(apiConfig.getSecretKey()).thenReturn("sk_test_mock");
        when(stripeProperties.getCurrency()).thenReturn("usd");
    }

    @Test
    @DisplayName("Complete Order Workflow: Create order from cart with payment intent")
    void completeOrderWorkflow_CreateFromCart_WithPayment() {
        Long cartId = 100L;
        CartDto cartDto = new CartDto(
                cartId,
                testUser.getId(),
                true,
                List.of(
                        new CartItemDto(1L, 1001L, 2),
                        new CartItemDto(2L, 1002L, 1)
                )
        );

        when(cartContextFacade.getCartById(cartId)).thenReturn(Optional.of(cartDto));

        when(orderStatusRepository.findByName(OrderStatuses.PENDING.name())).thenReturn(Optional.of(pendingStatus));

        doNothing().when(iamContextFacade).validateUserCanAccessResource(testUser.getId());
        when(iamContextFacade.userExists(testUser.getId())).thenReturn(true);
        doNothing().when(iamContextFacade).validateAddressBelongsToUser(testAddress.getId(), testUser.getId());

        when(productContextFacade.isProductDeleted(1001L)).thenReturn(false);
        when(productContextFacade.isProductActive(1001L)).thenReturn(true);
        when(productContextFacade.getProductPrice(1001L)).thenReturn(new BigDecimal("29.99"));
        when(productContextFacade.hasActiveSalePrice(1001L)).thenReturn(false);

        when(productContextFacade.isProductDeleted(1002L)).thenReturn(false);
        when(productContextFacade.isProductActive(1002L)).thenReturn(true);
        when(productContextFacade.getProductPrice(1002L)).thenReturn(new BigDecimal("49.99"));
        when(productContextFacade.hasActiveSalePrice(1002L)).thenReturn(false);

        doNothing().when(productContextFacade).decreaseProductStock(1001L, 2);
        doNothing().when(productContextFacade).decreaseProductStock(1002L, 1);
        when(productContextFacade.getProductStock(1001L)).thenReturn(50);
        when(productContextFacade.getProductStock(1002L)).thenReturn(30);

        PaymentIntentResponse paymentResponse = new PaymentIntentResponse(
                "pi_test_12345",
                "secret_test_12345",
                "requires_payment_method",
                null
        );

        Order mockOrder = new Order(testUser.getId(), cartId, testAddress.getId(), pendingStatus);
        setId(mockOrder, 1L);
        mockOrder.addItem(1001L, new BigDecimal("29.99"), 2, false);
        mockOrder.addItem(1002L, new BigDecimal("49.99"), 1, false);

        when(orderRepository.findByCartId(cartId)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                setId(order, 1L);
            }
            return order;
        });

        when(paymentProvider.initiatePayment(any(Order.class))).thenReturn(paymentResponse);
        when(paymentIntentStatusRepository.findByName(PaymentIntentStatuses.REQUIRES_PAYMENT_METHOD))
                .thenReturn(Optional.of(paymentIntentSucceededStatus));
        when(paymentIntentRepository.save(any(PaymentIntent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(cartContextFacade).checkoutCart(testUser.getId(), cartId);

        CreateOrderFromCartCommand command = new CreateOrderFromCartCommand(
                testUser.getId(),
                cartId,
                testAddress.getId(),
                null
        );

        Order createdOrder = orderCommandService.handle(command);

        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getId()).isEqualTo(1L);
        assertThat(createdOrder.getUserId()).isEqualTo(testUser.getId());
        assertThat(createdOrder.getCartId()).isEqualTo(cartId);
        assertThat(createdOrder.getItems()).hasSize(2);
        assertThat(createdOrder.getTotalAmount()).isGreaterThan(BigDecimal.ZERO);

        verify(productContextFacade).decreaseProductStock(1001L, 2);
        verify(productContextFacade).decreaseProductStock(1002L, 1);

        verify(paymentProvider).initiatePayment(any(Order.class));
        verify(paymentIntentRepository).save(any(PaymentIntent.class));

        verify(cartContextFacade).checkoutCart(testUser.getId(), cartId);
    }

    @Test
    @DisplayName("Order Workflow: Create order with discount code")
    void orderWorkflow_CreateWithDiscount() {
        Discount activeDiscount = new Discount(
                "SAVE20",
                20,
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(86400)
        );
        setId(activeDiscount, 1L);

        Long cartId = 200L;
        CartDto cartDto = new CartDto(
                cartId,
                testUser.getId(),
                true,
                List.of(new CartItemDto(1L, 2001L, 1))
        );

        when(cartContextFacade.getCartById(cartId)).thenReturn(Optional.of(cartDto));
        when(orderStatusRepository.findByName(OrderStatuses.PENDING.name())).thenReturn(Optional.of(pendingStatus));
        when(discountRepository.findByCode("SAVE20")).thenReturn(Optional.of(activeDiscount));

        doNothing().when(iamContextFacade).validateUserCanAccessResource(testUser.getId());
        when(iamContextFacade.userExists(testUser.getId())).thenReturn(true);
        doNothing().when(iamContextFacade).validateAddressBelongsToUser(testAddress.getId(), testUser.getId());

        when(productContextFacade.isProductDeleted(2001L)).thenReturn(false);
        when(productContextFacade.isProductActive(2001L)).thenReturn(true);
        when(productContextFacade.getProductPrice(2001L)).thenReturn(new BigDecimal("100.00"));
        when(productContextFacade.hasActiveSalePrice(2001L)).thenReturn(false);
        when(productContextFacade.getProductStock(2001L)).thenReturn(50);
        doNothing().when(productContextFacade).decreaseProductStock(2001L, 1);

        PaymentIntentResponse paymentResponse = new PaymentIntentResponse(
                "pi_discount_test",
                "secret_discount",
                "requires_payment_method",
                null
        );

        when(orderRepository.findByCartId(cartId)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                setId(order, 2L);
            }
            return order;
        });
        when(paymentProvider.initiatePayment(any(Order.class))).thenReturn(paymentResponse);
        when(paymentIntentStatusRepository.findByName(any())).thenReturn(Optional.of(paymentIntentSucceededStatus));
        when(paymentIntentRepository.save(any(PaymentIntent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(cartContextFacade).checkoutCart(testUser.getId(), cartId);

        CreateOrderFromCartCommand command = new CreateOrderFromCartCommand(
                testUser.getId(),
                cartId,
                testAddress.getId(),
                "SAVE20"
        );

        Order orderWithDiscount = orderCommandService.handle(command);

        assertThat(orderWithDiscount).isNotNull();
        assertThat(orderWithDiscount.getDiscount()).isNotNull();
        assertThat(orderWithDiscount.getDiscount().getCode()).isEqualTo("SAVE20");
        assertThat(orderWithDiscount.getTotalAmount()).isEqualByComparingTo(new BigDecimal("80.00"));

        verify(discountRepository).findByCode("SAVE20");
        verify(productContextFacade).decreaseProductStock(2001L, 1);
    }

    @Test
    @DisplayName("Order Workflow: Cancel order and restore stock")
    void orderWorkflow_CancelOrder_RestoreStock() {
        Order existingOrder = new Order(testUser.getId(), 300L, testAddress.getId(), pendingStatus);
        setId(existingOrder, 10L);
        setCreatedAt(existingOrder, Instant.now());
        existingOrder.addItem(3001L, new BigDecimal("199.99"), 2, false);
        existingOrder.addItem(3002L, new BigDecimal("79.99"), 1, false);
        existingOrder.setStripePaymentInfo("pi_cancel_test", "secret_cancel");

        when(orderRepository.findById(10L)).thenReturn(Optional.of(existingOrder));
        when(orderStatusRepository.findByName(OrderStatuses.CANCELLED.name())).thenReturn(Optional.of(cancelledStatus));

        doNothing().when(iamContextFacade).validateUserCanAccessResource(testUser.getId());
        doNothing().when(productContextFacade).increaseProductStock(3001L, 2);
        doNothing().when(productContextFacade).increaseProductStock(3002L, 1);
        doNothing().when(paymentProvider).cancelPayment("pi_cancel_test");

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(iamContextFacade.getUserEmail(testUser.getId())).thenReturn("client1@example.com");
        when(iamContextFacade.getUsernameById(testUser.getId())).thenReturn("client1");
        doNothing().when(notificationContextFacade).sendOrderStatusUpdate(
                anyString(), anyString(), anyLong(), anyString(), anyString(), anyString(), anyString()
        );

        CancelOrderCommand cancelCmd = new CancelOrderCommand(10L);
        Order cancelledOrder = orderCommandService.handle(cancelCmd);

        assertThat(cancelledOrder).isNotNull();
        assertThat(cancelledOrder.getStatus().getName()).isEqualTo(OrderStatuses.CANCELLED.name());

        verify(productContextFacade).increaseProductStock(3001L, 2);
        verify(productContextFacade).increaseProductStock(3002L, 1);

        verify(paymentProvider).cancelPayment("pi_cancel_test");

        verify(notificationContextFacade).sendOrderStatusUpdate(
                eq("client1@example.com"),
                eq("client1"),
                eq(10L),
                anyString(),
                contains("cancelled"),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("Order Workflow: Mark order as paid")
    void orderWorkflow_MarkOrderAsPaid() {
        Order pendingOrder = new Order(testUser.getId(), 400L, testAddress.getId(), pendingStatus);
        setId(pendingOrder, 20L);
        setCreatedAt(pendingOrder, Instant.now());
        pendingOrder.setStripePaymentInfo("pi_paid_test", "secret_paid");
        pendingOrder.addItem(4001L, new BigDecimal("299.99"), 1, false);

        when(orderRepository.findByStripePaymentIntentId("pi_paid_test")).thenReturn(Optional.of(pendingOrder));
        when(orderStatusRepository.findByName(OrderStatuses.PAID.name())).thenReturn(Optional.of(paidStatus));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(iamContextFacade.getUserEmail(testUser.getId())).thenReturn("client1@example.com");
        when(iamContextFacade.getUsernameById(testUser.getId())).thenReturn("client1");
        doNothing().when(notificationContextFacade).sendOrderStatusUpdate(
                anyString(), anyString(), anyLong(), anyString(), anyString(), anyString(), anyString()
        );

        MarkOrderAsPaidCommand markPaidCmd = new MarkOrderAsPaidCommand("pi_paid_test");
        Order paidOrder = orderCommandService.handle(markPaidCmd);

        assertThat(paidOrder).isNotNull();
        assertThat(paidOrder.isPaid()).isTrue();
        assertThat(paidOrder.getStatus().getName()).isEqualTo(OrderStatuses.PAID.name());
        assertThat(paidOrder.getPaidAt()).isNotNull();

        verify(orderRepository).save(any(Order.class));
        verify(notificationContextFacade).sendOrderStatusUpdate(
                eq("client1@example.com"),
                anyString(),
                eq(20L),
                anyString(),
                contains("payment"),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("Delivery Workflow: Update order through delivery stages")
    void deliveryWorkflow_UpdateThroughDeliveryStages() {
        Order paidOrder = new Order(testUser.getId(), 500L, testAddress.getId(), pendingStatus);
        setId(paidOrder, 30L);
        setCreatedAt(paidOrder, Instant.now());
        paidOrder.addItem(5001L, new BigDecimal("599.99"), 1, false);
        paidOrder.markAsPaid(paidStatus);

        when(orderRepository.findById(30L)).thenReturn(Optional.of(paidOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(iamContextFacade.getUserEmail(testUser.getId())).thenReturn("client1@example.com");
        when(iamContextFacade.getUsernameById(testUser.getId())).thenReturn("client1");
        doNothing().when(notificationContextFacade).sendOrderStatusUpdate(
                anyString(), anyString(), anyLong(), anyString(), anyString(), anyString(), anyString()
        );

        when(deliveryStatusRepository.findByName(DeliveryStatuses.PACKED.name())).thenReturn(Optional.of(packedStatus));
        UpdateOrderDeliveryStatusCommand packedCmd = new UpdateOrderDeliveryStatusCommand(30L, DeliveryStatuses.PACKED);
        Order packedOrder = orderCommandService.handle(packedCmd);

        assertThat(packedOrder.getDeliveryStatus()).isNotNull();
        assertThat(packedOrder.getDeliveryStatus().getName()).isEqualTo(DeliveryStatuses.PACKED.name());

        when(orderRepository.findById(30L)).thenReturn(Optional.of(packedOrder));
        when(deliveryStatusRepository.findByName(DeliveryStatuses.SHIPPED.name())).thenReturn(Optional.of(shippedStatus));
        UpdateOrderDeliveryStatusCommand shippedCmd = new UpdateOrderDeliveryStatusCommand(30L, DeliveryStatuses.SHIPPED);
        Order shippedOrder = orderCommandService.handle(shippedCmd);

        assertThat(shippedOrder.getDeliveryStatus().getName()).isEqualTo(DeliveryStatuses.SHIPPED.name());

        when(orderRepository.findById(30L)).thenReturn(Optional.of(shippedOrder));
        when(deliveryStatusRepository.findByName(DeliveryStatuses.IN_TRANSIT.name())).thenReturn(Optional.of(inTransitStatus));
        UpdateOrderDeliveryStatusCommand inTransitCmd = new UpdateOrderDeliveryStatusCommand(30L, DeliveryStatuses.IN_TRANSIT);
        Order inTransitOrder = orderCommandService.handle(inTransitCmd);

        assertThat(inTransitOrder.getDeliveryStatus().getName()).isEqualTo(DeliveryStatuses.IN_TRANSIT.name());

        when(orderRepository.findById(30L)).thenReturn(Optional.of(inTransitOrder));
        when(deliveryStatusRepository.findByName(DeliveryStatuses.DELIVERED.name())).thenReturn(Optional.of(deliveredStatus));
        UpdateOrderDeliveryStatusCommand deliveredCmd = new UpdateOrderDeliveryStatusCommand(30L, DeliveryStatuses.DELIVERED);
        Order deliveredOrder = orderCommandService.handle(deliveredCmd);

        assertThat(deliveredOrder.getDeliveryStatus().getName()).isEqualTo(DeliveryStatuses.DELIVERED.name());

        verify(notificationContextFacade, times(4)).sendOrderStatusUpdate(
                anyString(), anyString(), anyLong(), anyString(), anyString(), anyString(), anyString()
        );
    }

    @Test
    @DisplayName("Order Workflow: Cannot cancel paid order")
    void orderWorkflow_CannotCancelPaidOrder() {
        when(orderStatusRepository.findByName(OrderStatuses.CANCELLED.name())).thenReturn(Optional.of(cancelledStatus));
        doNothing().when(iamContextFacade).validateUserCanAccessResource(testUser.getId());

        Order paidOrder = new Order(testUser.getId(), 600L, testAddress.getId(), pendingStatus);
        setId(paidOrder, 40L);
        paidOrder.addItem(6001L, new BigDecimal("399.99"), 1, false);
        paidOrder.markAsPaid(paidStatus);

        when(orderRepository.findById(40L)).thenReturn(Optional.of(paidOrder));

        CancelOrderCommand cancelCmd = new CancelOrderCommand(40L);
        assertThatThrownBy(() -> orderCommandService.handle(cancelCmd))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Paid orders cannot be cancelled");
    }

    @Test
    @DisplayName("Order Workflow: Cannot update delivery status for unpaid order")
    void orderWorkflow_CannotUpdateDeliveryForUnpaidOrder() {
        Order pendingOrder = new Order(testUser.getId(), 700L, testAddress.getId(), pendingStatus);
        setId(pendingOrder, 50L);
        pendingOrder.addItem(7001L, new BigDecimal("199.99"), 1, false);

        when(orderRepository.findById(50L)).thenReturn(Optional.of(pendingOrder));
        when(deliveryStatusRepository.findByName(DeliveryStatuses.PACKED.name())).thenReturn(Optional.of(packedStatus));

        UpdateOrderDeliveryStatusCommand updateCmd = new UpdateOrderDeliveryStatusCommand(50L, DeliveryStatuses.PACKED);
        assertThatThrownBy(() -> orderCommandService.handle(updateCmd))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("PAID");
    }

    @Test
    @DisplayName("Order Workflow: Create order with products at sale price")
    void orderWorkflow_CreateOrder_WithSalePrice() {
        Long cartId = 800L;
        CartDto cartDto = new CartDto(
                cartId,
                testUser.getId(),
                true,
                List.of(
                        new CartItemDto(1L, 8001L, 2),
                        new CartItemDto(2L, 8002L, 1)
                )
        );

        when(cartContextFacade.getCartById(cartId)).thenReturn(Optional.of(cartDto));
        when(orderStatusRepository.findByName(OrderStatuses.PENDING.name())).thenReturn(Optional.of(pendingStatus));

        doNothing().when(iamContextFacade).validateUserCanAccessResource(testUser.getId());
        when(iamContextFacade.userExists(testUser.getId())).thenReturn(true);
        doNothing().when(iamContextFacade).validateAddressBelongsToUser(testAddress.getId(), testUser.getId());

        when(productContextFacade.isProductDeleted(8001L)).thenReturn(false);
        when(productContextFacade.isProductActive(8001L)).thenReturn(true);
        when(productContextFacade.getProductPrice(8001L)).thenReturn(new BigDecimal("50.00"));
        when(productContextFacade.hasActiveSalePrice(8001L)).thenReturn(false);
        when(productContextFacade.getProductStock(8001L)).thenReturn(100);
        doNothing().when(productContextFacade).decreaseProductStock(8001L, 2);

        when(productContextFacade.isProductDeleted(8002L)).thenReturn(false);
        when(productContextFacade.isProductActive(8002L)).thenReturn(true);
        when(productContextFacade.getProductPrice(8002L)).thenReturn(new BigDecimal("30.00"));
        when(productContextFacade.hasActiveSalePrice(8002L)).thenReturn(true);
        when(productContextFacade.getProductStock(8002L)).thenReturn(50);
        doNothing().when(productContextFacade).decreaseProductStock(8002L, 1);

        PaymentIntentResponse paymentResponse = new PaymentIntentResponse(
                "pi_sale_test",
                "secret_sale",
                "requires_payment_method",
                null
        );

        when(orderRepository.findByCartId(cartId)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                setId(order, 3L);
            }
            return order;
        });
        when(paymentProvider.initiatePayment(any(Order.class))).thenReturn(paymentResponse);
        when(paymentIntentStatusRepository.findByName(any())).thenReturn(Optional.of(paymentIntentSucceededStatus));
        when(paymentIntentRepository.save(any(PaymentIntent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(cartContextFacade).checkoutCart(testUser.getId(), cartId);

        CreateOrderFromCartCommand command = new CreateOrderFromCartCommand(
                testUser.getId(),
                cartId,
                testAddress.getId(),
                null
        );

        Order order = orderCommandService.handle(command);

        assertThat(order).isNotNull();
        assertThat(order.getItems()).hasSize(2);

        Optional<OrderItem> saleItem = order.getItems().stream()
                .filter(item -> item.getProductId().equals(8002L))
                .findFirst();

        assertThat(saleItem).isPresent();
        assertThat(saleItem.get().getIsPurchasedWithSalePrice()).isTrue();

        Optional<OrderItem> regularItem = order.getItems().stream()
                .filter(item -> item.getProductId().equals(8001L))
                .findFirst();

        assertThat(regularItem).isPresent();
        assertThat(regularItem.get().getIsPurchasedWithSalePrice()).isFalse();
    }

    @Test
    @DisplayName("Order Workflow: Create order triggers low stock notification")
    void orderWorkflow_CreateOrder_TriggersLowStockNotification() {
        Long cartId = 900L;
        CartDto cartDto = new CartDto(
                cartId,
                testUser.getId(),
                true,
                List.of(new CartItemDto(1L, 9001L, 3))
        );

        when(cartContextFacade.getCartById(cartId)).thenReturn(Optional.of(cartDto));
        when(orderStatusRepository.findByName(OrderStatuses.PENDING.name())).thenReturn(Optional.of(pendingStatus));

        doNothing().when(iamContextFacade).validateUserCanAccessResource(testUser.getId());
        when(iamContextFacade.userExists(testUser.getId())).thenReturn(true);
        doNothing().when(iamContextFacade).validateAddressBelongsToUser(testAddress.getId(), testUser.getId());

        when(productContextFacade.isProductDeleted(9001L)).thenReturn(false);
        when(productContextFacade.isProductActive(9001L)).thenReturn(true);
        when(productContextFacade.getProductPrice(9001L)).thenReturn(new BigDecimal("89.99"));
        when(productContextFacade.hasActiveSalePrice(9001L)).thenReturn(false);
        doNothing().when(productContextFacade).decreaseProductStock(9001L, 3);
        when(productContextFacade.getProductStock(9001L)).thenReturn(2);

        when(productContextFacade.getUsersWhoLikedProduct(9001L)).thenReturn(List.of(5L, 6L, 7L));
        when(productContextFacade.getProductName(9001L)).thenReturn("Almost Gone Item");
        when(iamContextFacade.getUserEmails(List.of(5L, 6L, 7L))).thenReturn(Map.of(
                5L, "user5@example.com",
                6L, "user6@example.com",
                7L, "user7@example.com"
        ));
        doNothing().when(notificationContextFacade).sendLowStockAlertBatch(anySet(), anyString(), anyInt());

        PaymentIntentResponse paymentResponse = new PaymentIntentResponse(
                "pi_lowstock_test",
                "secret_lowstock",
                "requires_payment_method",
                null
        );

        when(orderRepository.findByCartId(cartId)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                setId(order, 4L);
            }
            return order;
        });
        when(paymentProvider.initiatePayment(any(Order.class))).thenReturn(paymentResponse);
        when(paymentIntentStatusRepository.findByName(any())).thenReturn(Optional.of(paymentIntentSucceededStatus));
        when(paymentIntentRepository.save(any(PaymentIntent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(cartContextFacade).checkoutCart(testUser.getId(), cartId);

        CreateOrderFromCartCommand command = new CreateOrderFromCartCommand(
                testUser.getId(),
                cartId,
                testAddress.getId(),
                null
        );

        Order order = orderCommandService.handle(command);

        assertThat(order).isNotNull();
        verify(productContextFacade).decreaseProductStock(9001L, 3);
        verify(productContextFacade).getProductStock(9001L);
        verify(productContextFacade).getUsersWhoLikedProduct(9001L);
        verify(notificationContextFacade).sendLowStockAlertBatch(anySet(), eq("Almost Gone Item"), eq(2));
    }

    @Test
    @DisplayName("Order Workflow: Cannot create order from empty cart")
    void orderWorkflow_CannotCreateFromEmptyCart() {
        Long emptyCartId = 1000L;
        CartDto emptyCartDto = new CartDto(
                emptyCartId,
                testUser.getId(),
                true,
                List.of()
        );

        when(cartContextFacade.getCartById(emptyCartId)).thenReturn(Optional.of(emptyCartDto));
        doNothing().when(iamContextFacade).validateUserCanAccessResource(testUser.getId());
        when(iamContextFacade.userExists(testUser.getId())).thenReturn(true);
        doNothing().when(iamContextFacade).validateAddressBelongsToUser(testAddress.getId(), testUser.getId());

        CreateOrderFromCartCommand command = new CreateOrderFromCartCommand(
                testUser.getId(),
                emptyCartId,
                testAddress.getId(),
                null
        );

        assertThatThrownBy(() -> orderCommandService.handle(command))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("empty cart");
    }

    @Test
    @DisplayName("Order Workflow: Cannot create duplicate order from same cart")
    void orderWorkflow_CannotCreateDuplicateOrderFromSameCart() {
        Long cartId = 1100L;
        CartDto cartDto = new CartDto(
                cartId,
                testUser.getId(),
                true,
                List.of(new CartItemDto(1L, 11001L, 1))
        );

        Order existingOrder = new Order(testUser.getId(), cartId, testAddress.getId(), pendingStatus);
        setId(existingOrder, 100L);

        when(cartContextFacade.getCartById(cartId)).thenReturn(Optional.of(cartDto));
        doNothing().when(iamContextFacade).validateUserCanAccessResource(testUser.getId());
        when(iamContextFacade.userExists(testUser.getId())).thenReturn(true);
        doNothing().when(iamContextFacade).validateAddressBelongsToUser(testAddress.getId(), testUser.getId());
        when(orderRepository.findByCartId(cartId)).thenReturn(Optional.of(existingOrder));

        CreateOrderFromCartCommand command = new CreateOrderFromCartCommand(
                testUser.getId(),
                cartId,
                testAddress.getId(),
                null
        );

        assertThatThrownBy(() -> orderCommandService.handle(command))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Order Workflow: Discount not applied to items with sale price")
    void orderWorkflow_DiscountNotAppliedToSaleItems() {
        Long cartId = 1200L;
        CartDto cartDto = new CartDto(
                cartId,
                testUser.getId(),
                true,
                List.of(
                        new CartItemDto(1L, 12001L, 1),
                        new CartItemDto(2L, 12002L, 1)
                )
        );

        Discount discount = new Discount(
                "EXTRA10",
                10,
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(86400)
        );
        setId(discount, 2L);

        when(cartContextFacade.getCartById(cartId)).thenReturn(Optional.of(cartDto));
        when(orderStatusRepository.findByName(OrderStatuses.PENDING.name())).thenReturn(Optional.of(pendingStatus));
        when(discountRepository.findByCode("EXTRA10")).thenReturn(Optional.of(discount));

        doNothing().when(iamContextFacade).validateUserCanAccessResource(testUser.getId());
        when(iamContextFacade.userExists(testUser.getId())).thenReturn(true);
        doNothing().when(iamContextFacade).validateAddressBelongsToUser(testAddress.getId(), testUser.getId());

        when(productContextFacade.isProductDeleted(12001L)).thenReturn(false);
        when(productContextFacade.isProductActive(12001L)).thenReturn(true);
        when(productContextFacade.getProductPrice(12001L)).thenReturn(new BigDecimal("100.00"));
        when(productContextFacade.hasActiveSalePrice(12001L)).thenReturn(false);
        when(productContextFacade.getProductStock(12001L)).thenReturn(50);
        doNothing().when(productContextFacade).decreaseProductStock(12001L, 1);

        when(productContextFacade.isProductDeleted(12002L)).thenReturn(false);
        when(productContextFacade.isProductActive(12002L)).thenReturn(true);
        when(productContextFacade.getProductPrice(12002L)).thenReturn(new BigDecimal("50.00"));
        when(productContextFacade.hasActiveSalePrice(12002L)).thenReturn(true);
        when(productContextFacade.getProductStock(12002L)).thenReturn(30);
        doNothing().when(productContextFacade).decreaseProductStock(12002L, 1);

        PaymentIntentResponse paymentResponse = new PaymentIntentResponse(
                "pi_mixed_test",
                "secret_mixed",
                "requires_payment_method",
                null
        );

        when(orderRepository.findByCartId(cartId)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                setId(order, 5L);
            }
            return order;
        });
        when(paymentProvider.initiatePayment(any(Order.class))).thenReturn(paymentResponse);
        when(paymentIntentStatusRepository.findByName(any())).thenReturn(Optional.of(paymentIntentSucceededStatus));
        when(paymentIntentRepository.save(any(PaymentIntent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(cartContextFacade).checkoutCart(testUser.getId(), cartId);

        CreateOrderFromCartCommand command = new CreateOrderFromCartCommand(
                testUser.getId(),
                cartId,
                testAddress.getId(),
                "EXTRA10"
        );

        Order order = orderCommandService.handle(command);

        assertThat(order).isNotNull();
        assertThat(order.getDiscount()).isNotNull();

        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("140.00"));

        verify(productContextFacade).decreaseProductStock(12001L, 1);
        verify(productContextFacade).decreaseProductStock(12002L, 1);
    }

    @Test
    @DisplayName("Order Workflow: Multiple items same product reduces stock correctly")
    void orderWorkflow_MultipleQuantity_ReducesStockCorrectly() {
        Long cartId = 1300L;
        CartDto cartDto = new CartDto(
                cartId,
                testUser.getId(),
                true,
                List.of(new CartItemDto(1L, 13001L, 5))
        );

        when(cartContextFacade.getCartById(cartId)).thenReturn(Optional.of(cartDto));
        when(orderStatusRepository.findByName(OrderStatuses.PENDING.name())).thenReturn(Optional.of(pendingStatus));

        doNothing().when(iamContextFacade).validateUserCanAccessResource(testUser.getId());
        when(iamContextFacade.userExists(testUser.getId())).thenReturn(true);
        doNothing().when(iamContextFacade).validateAddressBelongsToUser(testAddress.getId(), testUser.getId());

        when(productContextFacade.isProductDeleted(13001L)).thenReturn(false);
        when(productContextFacade.isProductActive(13001L)).thenReturn(true);
        when(productContextFacade.getProductPrice(13001L)).thenReturn(new BigDecimal("15.99"));
        when(productContextFacade.hasActiveSalePrice(13001L)).thenReturn(false);
        doNothing().when(productContextFacade).decreaseProductStock(13001L, 5);
        when(productContextFacade.getProductStock(13001L)).thenReturn(15);

        PaymentIntentResponse paymentResponse = new PaymentIntentResponse(
                "pi_multi_test",
                "secret_multi",
                "requires_payment_method",
                null
        );

        when(orderRepository.findByCartId(cartId)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                setId(order, 6L);
            }
            return order;
        });
        when(paymentProvider.initiatePayment(any(Order.class))).thenReturn(paymentResponse);
        when(paymentIntentStatusRepository.findByName(any())).thenReturn(Optional.of(paymentIntentSucceededStatus));
        when(paymentIntentRepository.save(any(PaymentIntent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(cartContextFacade).checkoutCart(testUser.getId(), cartId);

        CreateOrderFromCartCommand command = new CreateOrderFromCartCommand(
                testUser.getId(),
                cartId,
                testAddress.getId(),
                null
        );

        Order order = orderCommandService.handle(command);

        assertThat(order).isNotNull();
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getQuantity()).isEqualTo(5);

        verify(productContextFacade).decreaseProductStock(13001L, 5);
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = null;
            Class<?> currentClass = entity.getClass();

            while (currentClass != null && field == null) {
                try {
                    field = currentClass.getDeclaredField("id");
                } catch (NoSuchFieldException e) {
                    currentClass = currentClass.getSuperclass();
                }
            }

            if (field == null) {
                throw new NoSuchFieldException("Field 'id' not found in class hierarchy of " + entity.getClass().getName());
            }

            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID: " + e.getMessage(), e);
        }
    }

    private void setCreatedAt(Order order, Instant createdAt) {
        try {
            Field field = null;
            Class<?> currentClass = order.getClass();

            while (currentClass != null && field == null) {
                try {
                    field = currentClass.getDeclaredField("createdAt");
                } catch (NoSuchFieldException e) {
                    currentClass = currentClass.getSuperclass();
                }
            }

            if (field != null) {
                field.setAccessible(true);
                field.set(order, createdAt);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set createdAt: " + e.getMessage(), e);
        }
    }
}



