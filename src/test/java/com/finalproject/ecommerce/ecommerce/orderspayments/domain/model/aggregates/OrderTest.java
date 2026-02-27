package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.DeliveryStatus;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.Discount;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.OrderStatus;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.DeliveryStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.OrderStatuses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order Aggregate")
class OrderTest {

    private Order order;
    private OrderStatus pendingStatus;
    private OrderStatus paidStatus;
    private OrderStatus cancelledStatus;

    @BeforeEach
    void setUp() {
        pendingStatus = new OrderStatus(OrderStatuses.PENDING, "Pending");
        paidStatus = new OrderStatus(OrderStatuses.PAID, "Paid");
        cancelledStatus = new OrderStatus(OrderStatuses.CANCELLED, "Cancelled");
        order = new Order(1L, 10L, 100L, pendingStatus);
    }

    @Nested
    @DisplayName("Creation")
    class CreationTests {

        @Test
        @DisplayName("should create order with correct fields")
        void shouldCreateOrder() {
            assertThat(order.getUserId()).isEqualTo(1L);
            assertThat(order.getCartId()).isEqualTo(10L);
            assertThat(order.getAddressId()).isEqualTo(100L);
            assertThat(order.getStatus()).isEqualTo(pendingStatus);
            assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(order.getItems()).isEmpty();
            assertThat(order.isPending()).isTrue();
        }
    }


    @Nested
    @DisplayName("Add Item")
    class AddItemTests {

        @Test
        @DisplayName("should add item and recalculate total")
        void shouldAddItemAndRecalculate() {
            order.addItem(200L, new BigDecimal("25.00"), 2, false);

            assertThat(order.getItems()).hasSize(1);
            assertThat(order.getTotalAmount()).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("should add multiple items and sum totals")
        void shouldAddMultipleItems() {
            order.addItem(200L, new BigDecimal("10.00"), 2, false);
            order.addItem(300L, new BigDecimal("5.00"), 3, false);

            assertThat(order.getItems()).hasSize(2);
            assertThat(order.getTotalAmount()).isEqualByComparingTo("35.00");
        }
    }

    @Nested
    @DisplayName("Mark As Paid")
    class MarkAsPaidTests {

        @Test
        @DisplayName("should mark pending order as paid")
        void shouldMarkAsPaid() {
            order.markAsPaid(paidStatus);

            assertThat(order.isPaid()).isTrue();
            assertThat(order.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw when order is not pending")
        void shouldThrowWhenNotPending() {
            order.markAsPaid(paidStatus);

            assertThatThrownBy(() -> order.markAsPaid(paidStatus))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only pending orders");
        }
    }

    @Nested
    @DisplayName("Cancel Order")
    class CancelTests {

        @Test
        @DisplayName("should cancel pending order")
        void shouldCancelPendingOrder() {
            order.cancel(cancelledStatus);

            assertThat(order.isCancelled()).isTrue();
        }

        @Test
        @DisplayName("should throw when cancelling paid order")
        void shouldThrowWhenPaid() {
            order.markAsPaid(paidStatus);

            assertThatThrownBy(() -> order.cancel(cancelledStatus))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Paid orders cannot be cancelled");
        }

        @Test
        @DisplayName("should throw when cancelling delivered order")
        void shouldThrowWhenDelivered() {
            order.markAsPaid(paidStatus);
            order.updateDeliveryStatus(new DeliveryStatus(DeliveryStatuses.PACKED, "Packed"));
            order.updateDeliveryStatus(new DeliveryStatus(DeliveryStatuses.SHIPPED, "Shipped"));
            order.updateDeliveryStatus(new DeliveryStatus(DeliveryStatuses.IN_TRANSIT, "In Transit"));
            order.updateDeliveryStatus(new DeliveryStatus(DeliveryStatuses.DELIVERED, "Delivered"));

            assertThatThrownBy(() -> order.cancel(cancelledStatus))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Stripe Payment Info")
    class StripePaymentInfoTests {

        @Test
        @DisplayName("should set stripe payment info")
        void shouldSetStripeInfo() {
            order.setStripePaymentInfo("pi_123", "secret_456");

            assertThat(order.getStripePaymentIntentId()).isEqualTo("pi_123");
            assertThat(order.getStripeClientSecret()).isEqualTo("secret_456");
        }
    }

    @Nested
    @DisplayName("Update Delivery Status")
    class UpdateDeliveryStatusTests {

        @Test
        @DisplayName("should set initial delivery status to PACKED for paid order")
        void shouldSetInitialStatusToPacked() {
            order.markAsPaid(paidStatus);
            DeliveryStatus packedStatus = new DeliveryStatus(DeliveryStatuses.PACKED, "Packed");

            order.updateDeliveryStatus(packedStatus);

            assertThat(order.getDeliveryStatus()).isEqualTo(packedStatus);
        }

        @Test
        @DisplayName("should follow full valid transition sequence PACKED → SHIPPED → IN_TRANSIT → DELIVERED")
        void shouldFollowFullValidSequence() {
            order.markAsPaid(paidStatus);
            DeliveryStatus packed = new DeliveryStatus(DeliveryStatuses.PACKED, "Packed");
            DeliveryStatus shipped = new DeliveryStatus(DeliveryStatuses.SHIPPED, "Shipped");
            DeliveryStatus inTransit = new DeliveryStatus(DeliveryStatuses.IN_TRANSIT, "In Transit");
            DeliveryStatus delivered = new DeliveryStatus(DeliveryStatuses.DELIVERED, "Delivered");

            order.updateDeliveryStatus(packed);
            order.updateDeliveryStatus(shipped);
            order.updateDeliveryStatus(inTransit);
            order.updateDeliveryStatus(delivered);

            assertThat(order.getDeliveryStatus()).isEqualTo(delivered);
        }

        @Test
        @DisplayName("should throw when setting initial status to SHIPPED (skipping PACKED)")
        void shouldThrowWhenInitialStatusIsNotPacked() {
            order.markAsPaid(paidStatus);
            DeliveryStatus shippedStatus = new DeliveryStatus(DeliveryStatuses.SHIPPED, "Shipped");

            assertThatThrownBy(() -> order.updateDeliveryStatus(shippedStatus))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Initial delivery status must be");
        }

        @Test
        @DisplayName("should throw when setting initial status to DELIVERED directly")
        void shouldThrowWhenInitialStatusIsDelivered() {
            order.markAsPaid(paidStatus);
            DeliveryStatus deliveredStatus = new DeliveryStatus(DeliveryStatuses.DELIVERED, "Delivered");

            assertThatThrownBy(() -> order.updateDeliveryStatus(deliveredStatus))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Initial delivery status must be");
        }

        @Test
        @DisplayName("should throw when transitioning from PACKED directly to DELIVERED")
        void shouldThrowWhenSkippingFromPackedToDelivered() {
            order.markAsPaid(paidStatus);
            order.updateDeliveryStatus(new DeliveryStatus(DeliveryStatuses.PACKED, "Packed"));
            DeliveryStatus deliveredStatus = new DeliveryStatus(DeliveryStatuses.DELIVERED, "Delivered");

            assertThatThrownBy(() -> order.updateDeliveryStatus(deliveredStatus))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid delivery status transition from PACKED to DELIVERED");
        }

        @Test
        @DisplayName("should throw when transitioning from PACKED directly to IN_TRANSIT")
        void shouldThrowWhenSkippingFromPackedToInTransit() {
            order.markAsPaid(paidStatus);
            order.updateDeliveryStatus(new DeliveryStatus(DeliveryStatuses.PACKED, "Packed"));
            DeliveryStatus inTransitStatus = new DeliveryStatus(DeliveryStatuses.IN_TRANSIT, "In Transit");

            assertThatThrownBy(() -> order.updateDeliveryStatus(inTransitStatus))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid delivery status transition from PACKED to IN_TRANSIT");
        }

        @Test
        @DisplayName("should throw when transitioning from SHIPPED directly to DELIVERED")
        void shouldThrowWhenSkippingFromShippedToDelivered() {
            order.markAsPaid(paidStatus);
            order.updateDeliveryStatus(new DeliveryStatus(DeliveryStatuses.PACKED, "Packed"));
            order.updateDeliveryStatus(new DeliveryStatus(DeliveryStatuses.SHIPPED, "Shipped"));
            DeliveryStatus deliveredStatus = new DeliveryStatus(DeliveryStatuses.DELIVERED, "Delivered");

            assertThatThrownBy(() -> order.updateDeliveryStatus(deliveredStatus))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid delivery status transition from SHIPPED to DELIVERED");
        }

        @Test
        @DisplayName("should throw when going backwards from SHIPPED to PACKED")
        void shouldThrowWhenGoingBackwards() {
            order.markAsPaid(paidStatus);
            order.updateDeliveryStatus(new DeliveryStatus(DeliveryStatuses.PACKED, "Packed"));
            order.updateDeliveryStatus(new DeliveryStatus(DeliveryStatuses.SHIPPED, "Shipped"));
            DeliveryStatus packedStatus = new DeliveryStatus(DeliveryStatuses.PACKED, "Packed");

            assertThatThrownBy(() -> order.updateDeliveryStatus(packedStatus))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid delivery status transition from SHIPPED to PACKED");
        }

        @Test
        @DisplayName("should throw when order is not paid")
        void shouldThrowWhenNotPaid() {
            DeliveryStatus packedStatus = new DeliveryStatus(DeliveryStatuses.PACKED, "Packed");

            assertThatThrownBy(() -> order.updateDeliveryStatus(packedStatus))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only paid orders");
        }

        @Test
        @DisplayName("should throw when order is already delivered")
        void shouldThrowWhenAlreadyDelivered() {
            order.markAsPaid(paidStatus);
            order.updateDeliveryStatus(new DeliveryStatus(DeliveryStatuses.PACKED, "Packed"));
            order.updateDeliveryStatus(new DeliveryStatus(DeliveryStatuses.SHIPPED, "Shipped"));
            order.updateDeliveryStatus(new DeliveryStatus(DeliveryStatuses.IN_TRANSIT, "In Transit"));
            order.updateDeliveryStatus(new DeliveryStatus(DeliveryStatuses.DELIVERED, "Delivered"));
            DeliveryStatus shippedStatus = new DeliveryStatus(DeliveryStatuses.SHIPPED, "Shipped");

            assertThatThrownBy(() -> order.updateDeliveryStatus(shippedStatus))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already been delivered");
        }
    }

    @Nested
    @DisplayName("Status Checks")
    class StatusCheckTests {

        @Test
        @DisplayName("should report isPending correctly")
        void shouldReportIsPending() {
            assertThat(order.isPending()).isTrue();
            assertThat(order.isPaid()).isFalse();
            assertThat(order.isCancelled()).isFalse();
        }

        @Test
        @DisplayName("should report isPaid correctly")
        void shouldReportIsPaid() {
            order.markAsPaid(paidStatus);

            assertThat(order.isPaid()).isTrue();
            assertThat(order.isPending()).isFalse();
        }

        @Test
        @DisplayName("should report isCancelled correctly")
        void shouldReportIsCancelled() {
            order.cancel(cancelledStatus);

            assertThat(order.isCancelled()).isTrue();
            assertThat(order.isPending()).isFalse();
        }
    }

    @Nested
    @DisplayName("Discount Application")
    class DiscountApplicationTests {

        @Test
        @DisplayName("should apply discount only to items without sale price")
        void shouldApplyDiscountOnlyToItemsWithoutSalePrice() {
            order.addItem(200L, new BigDecimal("100.00"), 1, false);

            order.addItem(300L, new BigDecimal("50.00"), 1, true);

            assertThat(order.getTotalAmount()).isEqualByComparingTo("150.00");

            Discount discount = new Discount("SAVE20", 20,
                    Instant.now(),
                    Instant.now().plus(7, ChronoUnit.DAYS));
            order.applyDiscount(discount);

            assertThat(order.getTotalAmount()).isEqualByComparingTo("130.00");
        }

        @Test
        @DisplayName("should not apply discount when all items have sale price")
        void shouldNotApplyDiscountWhenAllItemsHaveSalePrice() {
            order.addItem(200L, new BigDecimal("50.00"), 2, true);
            order.addItem(300L, new BigDecimal("30.00"), 1, true);

            assertThat(order.getTotalAmount()).isEqualByComparingTo("130.00");

            Discount discount = new Discount("SAVE30", 30,
                    Instant.now(),
                    Instant.now().plus(7, ChronoUnit.DAYS));
            order.applyDiscount(discount);

            assertThat(order.getTotalAmount()).isEqualByComparingTo("130.00");
        }

        @Test
        @DisplayName("should apply full discount when no items have sale price")
        void shouldApplyFullDiscountWhenNoItemsHaveSalePrice() {
            order.addItem(200L, new BigDecimal("100.00"), 1, false);
            order.addItem(300L, new BigDecimal("50.00"), 2, false);

            assertThat(order.getTotalAmount()).isEqualByComparingTo("200.00");

            Discount discount = new Discount("SAVE25", 25,
                    Instant.now(),
                    Instant.now().plus(7, ChronoUnit.DAYS));
            order.applyDiscount(discount);

            assertThat(order.getTotalAmount()).isEqualByComparingTo("150.00");
        }

        @Test
        @DisplayName("should handle multiple items with mixed sale status correctly")
        void shouldHandleMixedItemsCorrectly() {
            order.addItem(100L, new BigDecimal("20.00"), 3, false);
            order.addItem(200L, new BigDecimal("30.00"), 2, false);

            order.addItem(300L, new BigDecimal("15.00"), 4, true);
            order.addItem(400L, new BigDecimal("25.00"), 2, true);

            assertThat(order.getTotalAmount()).isEqualByComparingTo("230.00");

            Discount discount = new Discount("SAVE10", 10,
                    Instant.now(),
                    Instant.now().plus(7, ChronoUnit.DAYS));
            order.applyDiscount(discount);

            assertThat(order.getTotalAmount()).isEqualByComparingTo("218.00");
        }

        @Test
        @DisplayName("should recalculate discount when adding more items")
        void shouldRecalculateDiscountWhenAddingMoreItems() {
            order.addItem(100L, new BigDecimal("100.00"), 1, false);

            Discount discount = new Discount("SAVE20", 20,
                    Instant.now(),
                    Instant.now().plus(7, ChronoUnit.DAYS));
            order.applyDiscount(discount);

            assertThat(order.getTotalAmount()).isEqualByComparingTo("80.00");

            order.addItem(200L, new BigDecimal("50.00"), 1, false);

            assertThat(order.getTotalAmount()).isEqualByComparingTo("120.00");
        }
    }
}

