package com.finalproject.ecommerce.ecommerce.carts.domain.model.entities;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.valueobjects.CartStatuses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CartItem Entity")
class CartItemTest {

    private Cart cart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        CartStatus activeStatus = new CartStatus(CartStatuses.ACTIVE, "Active");
        cart = new Cart(1L, activeStatus);
        cartItem = new CartItem(cart, 100L, 5);
    }

    @Nested
    @DisplayName("Creation")
    class CreationTests {

        @Test
        @DisplayName("should create cart item with correct fields")
        void shouldCreateCartItem() {
            assertThat(cartItem.getCart()).isEqualTo(cart);
            assertThat(cartItem.getProductId()).isEqualTo(100L);
            assertThat(cartItem.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("should throw when quantity is null")
        void shouldThrowWhenQuantityNull() {
            assertThatThrownBy(() -> new CartItem(cart, 100L, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than 0");
        }

        @Test
        @DisplayName("should throw when quantity is zero")
        void shouldThrowWhenQuantityZero() {
            assertThatThrownBy(() -> new CartItem(cart, 100L, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than 0");
        }

        @Test
        @DisplayName("should throw when productId is null")
        void shouldThrowWhenProductIdNull() {
            assertThatThrownBy(() -> new CartItem(cart, null, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product ID cannot be null");
        }
    }


    @Nested
    @DisplayName("Increase Quantity")
    class IncreaseQuantityTests {

        @Test
        @DisplayName("should increase quantity by amount")
        void shouldIncreaseQuantity() {
            cartItem.increaseQuantity(3);

            assertThat(cartItem.getQuantity()).isEqualTo(8);
        }

        @Test
        @DisplayName("should throw when amount is null")
        void shouldThrowWhenAmountNull() {
            assertThatThrownBy(() -> cartItem.increaseQuantity(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be greater than 0");
        }

        @Test
        @DisplayName("should throw when amount is zero")
        void shouldThrowWhenAmountZero() {
            assertThatThrownBy(() -> cartItem.increaseQuantity(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be greater than 0");
        }
    }

    @Nested
    @DisplayName("Update Quantity")
    class UpdateQuantityTests {

        @Test
        @DisplayName("should update quantity to new value")
        void shouldUpdateQuantity() {
            cartItem.updateQuantity(10);

            assertThat(cartItem.getQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("should throw when new quantity is null")
        void shouldThrowWhenNull() {
            assertThatThrownBy(() -> cartItem.updateQuantity(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than 0");
        }

        @Test
        @DisplayName("should throw when new quantity is zero")
        void shouldThrowWhenZero() {
            assertThatThrownBy(() -> cartItem.updateQuantity(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than 0");
        }
    }

    @Nested
    @DisplayName("Decrease Quantity")
    class DecreaseQuantityTests {

        @Test
        @DisplayName("should decrease quantity by amount")
        void shouldDecreaseQuantity() {
            cartItem.decreaseQuantity(2);

            assertThat(cartItem.getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("should throw when amount is null")
        void shouldThrowWhenAmountNull() {
            assertThatThrownBy(() -> cartItem.decreaseQuantity(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be greater than 0");
        }

        @Test
        @DisplayName("should throw when amount is zero")
        void shouldThrowWhenAmountZero() {
            assertThatThrownBy(() -> cartItem.decreaseQuantity(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be greater than 0");
        }

        @Test
        @DisplayName("should throw when decreasing below zero")
        void shouldThrowWhenBelowZero() {
            assertThatThrownBy(() -> cartItem.decreaseQuantity(6))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot decrease quantity below 0");
        }
    }

    @Nested
    @DisplayName("isForProduct")
    class IsForProductTests {

        @Test
        @DisplayName("should return true for matching productId")
        void shouldReturnTrueForMatch() {
            boolean result = cartItem.isForProduct(100L);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false for different productId")
        void shouldReturnFalseForDifferent() {
            boolean result = cartItem.isForProduct(999L);

            assertThat(result).isFalse();
        }
    }
}

