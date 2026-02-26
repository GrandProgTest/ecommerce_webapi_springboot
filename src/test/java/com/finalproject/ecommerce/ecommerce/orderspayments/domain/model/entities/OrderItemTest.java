package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.OrderStatuses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderItem Entity")
class OrderItemTest {

    private Order order;

    @BeforeEach
    void setUp() {
        OrderStatus pendingStatus = new OrderStatus(OrderStatuses.PENDING, "Pending");
        order = new Order(1L, 10L, 100L, pendingStatus);
    }

    @Nested
    @DisplayName("Creation")
    class CreationTests {

        @Test
        @DisplayName("should create order item with correct fields")
        void shouldCreateOrderItem() {
            BigDecimal price = new BigDecimal("25.50");

            OrderItem item = new OrderItem(order, 200L, price, 3);

            assertThat(item.getOrder()).isEqualTo(order);
            assertThat(item.getProductId()).isEqualTo(200L);
            assertThat(item.getPriceAtPurchase()).isEqualByComparingTo("25.50");
            assertThat(item.getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("should throw when productId is null")
        void shouldThrowWhenProductIdNull() {
            assertThatThrownBy(() -> new OrderItem(order, null, BigDecimal.TEN, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product ID cannot be null");
        }

        @Test
        @DisplayName("should throw when price is null")
        void shouldThrowWhenPriceNull() {
            assertThatThrownBy(() -> new OrderItem(order, 200L, null, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Price must be non-negative");
        }

        @Test
        @DisplayName("should throw when price is negative")
        void shouldThrowWhenPriceNegative() {
            assertThatThrownBy(() -> new OrderItem(order, 200L, new BigDecimal("-1"), 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Price must be non-negative");
        }

        @Test
        @DisplayName("should throw when quantity is null")
        void shouldThrowWhenQuantityNull() {
            assertThatThrownBy(() -> new OrderItem(order, 200L, BigDecimal.TEN, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be positive");
        }

        @Test
        @DisplayName("should throw when quantity is zero")
        void shouldThrowWhenQuantityZero() {
            assertThatThrownBy(() -> new OrderItem(order, 200L, BigDecimal.TEN, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be positive");
        }

        @Test
        @DisplayName("should accept price of zero")
        void shouldAcceptZeroPrice() {
            OrderItem item = new OrderItem(order, 200L, BigDecimal.ZERO, 1);

            assertThat(item.getPriceAtPurchase()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Subtotal Calculation")
    class SubtotalTests {

        @Test
        @DisplayName("should calculate subtotal as price * quantity")
        void shouldCalculateSubtotal() {
            OrderItem item = new OrderItem(order, 200L, new BigDecimal("15.00"), 4);

            BigDecimal subtotal = item.getSubtotal();

            assertThat(subtotal).isEqualByComparingTo("60.00");
        }

        @Test
        @DisplayName("should return zero subtotal when price is zero")
        void shouldReturnZeroSubtotal() {
            OrderItem item = new OrderItem(order, 200L, BigDecimal.ZERO, 5);

            BigDecimal subtotal = item.getSubtotal();

            assertThat(subtotal).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}

