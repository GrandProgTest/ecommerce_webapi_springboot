package com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates;

import com.finalproject.ecommerce.ecommerce.carts.domain.exceptions.InvalidCartOperationException;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.entities.CartStatus;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.valueobjects.CartStatuses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Cart Aggregate")
class CartTest {

    private Cart cart;
    private CartStatus activeStatus;
    private CartStatus checkedOutStatus;
    private CartStatus abandonedStatus;

    @BeforeEach
    void setUp() {
        activeStatus = new CartStatus(CartStatuses.ACTIVE, "Active cart");
        checkedOutStatus = new CartStatus(CartStatuses.CHECKED_OUT, "Checked out");
        abandonedStatus = new CartStatus(CartStatuses.ABANDONED, "Abandoned");
        cart = new Cart(1L, activeStatus);
    }


    @Nested
    @DisplayName("Creation")
    class CreationTests {

        @Test
        @DisplayName("should create cart with userId and active status")
        void shouldCreateWithUserIdAndStatus() {
            assertThat(cart.getUserId()).isEqualTo(1L);
            assertThat(cart.getStatus()).isEqualTo(activeStatus);
            assertThat(cart.getItems()).isEmpty();
        }

        @Test
        @DisplayName("should create cart with userId only")
        void shouldCreateWithUserIdOnly() {
            Cart simpleCart = new Cart(10L);

            assertThat(simpleCart.getUserId()).isEqualTo(10L);
            assertThat(simpleCart.getItems()).isEmpty();
        }

        @Test
        @DisplayName("should throw when userId is null")
        void shouldThrowWhenUserIdNull() {
            assertThatThrownBy(() -> new Cart(null, activeStatus))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User ID cannot be null");
        }

        @Test
        @DisplayName("should throw when userId is null (single arg constructor)")
        void shouldThrowWhenUserIdNullSingleArg() {
            assertThatThrownBy(() -> new Cart(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("User ID cannot be null");
        }
    }

    @Nested
    @DisplayName("Add Product")
    class AddProductTests {

        @Test
        @DisplayName("should add new product to cart")
        void shouldAddNewProduct() {
            cart.addProduct(100L, 3);

            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.containsProduct(100L)).isTrue();
            assertThat(cart.getProductQuantity(100L)).isEqualTo(3);
        }

        @Test
        @DisplayName("should increase quantity when adding existing product")
        void shouldIncreaseQuantityForExistingProduct() {
            cart.addProduct(100L, 2);

            cart.addProduct(100L, 3);

            assertThat(cart.getItems()).hasSize(1);
            assertThat(cart.getProductQuantity(100L)).isEqualTo(5);
        }

        @Test
        @DisplayName("should throw when productId is null")
        void shouldThrowWhenProductIdNull() {
            assertThatThrownBy(() -> cart.addProduct(null, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Product ID cannot be null");
        }

        @Test
        @DisplayName("should throw when quantity is zero")
        void shouldThrowWhenQuantityZero() {
            assertThatThrownBy(() -> cart.addProduct(100L, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than 0");
        }

        @Test
        @DisplayName("should throw when quantity is negative")
        void shouldThrowWhenQuantityNegative() {
            assertThatThrownBy(() -> cart.addProduct(100L, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than 0");
        }

        @Test
        @DisplayName("should throw when cart is not active")
        void shouldThrowWhenCartNotActive() {
            cart.addProduct(200L, 1);
            cart.checkout(checkedOutStatus);

            assertThatThrownBy(() -> cart.addProduct(100L, 1))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("Cannot modify cart");
        }
    }

    @Nested
    @DisplayName("Update Product Quantity")
    class UpdateProductQuantityTests {

        @BeforeEach
        void addItem() {
            cart.addProduct(100L, 3);
        }

        @Test
        @DisplayName("should update quantity of existing product")
        void shouldUpdateQuantity() {
            cart.updateProductQuantity(100L, 5);

            assertThat(cart.getProductQuantity(100L)).isEqualTo(5);
        }

        @Test
        @DisplayName("should throw when product not in cart")
        void shouldThrowWhenProductNotInCart() {
            assertThatThrownBy(() -> cart.updateProductQuantity(999L, 1))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("Product not found in cart");
        }

        @Test
        @DisplayName("should throw when new quantity is zero")
        void shouldThrowWhenQuantityZero() {
            assertThatThrownBy(() -> cart.updateProductQuantity(100L, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than 0");
        }

        @Test
        @DisplayName("should throw when cart is not active")
        void shouldThrowWhenNotActive() {
            cart.checkout(checkedOutStatus);

            assertThatThrownBy(() -> cart.updateProductQuantity(100L, 5))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("Cannot modify cart");
        }
    }

    @Nested
    @DisplayName("Remove Product")
    class RemoveProductTests {

        @BeforeEach
        void addItem() {
            cart.addProduct(100L, 2);
        }

        @Test
        @DisplayName("should remove product from cart")
        void shouldRemoveProduct() {
            cart.removeProduct(100L);

            assertThat(cart.containsProduct(100L)).isFalse();
            assertThat(cart.getItems()).isEmpty();
        }

        @Test
        @DisplayName("should throw when product not in cart")
        void shouldThrowWhenProductNotFound() {
            assertThatThrownBy(() -> cart.removeProduct(999L))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("Product not found in cart");
        }

        @Test
        @DisplayName("should throw when cart is not active")
        void shouldThrowWhenNotActive() {
            cart.checkout(checkedOutStatus);

            assertThatThrownBy(() -> cart.removeProduct(100L))
                    .isInstanceOf(InvalidCartOperationException.class);
        }
    }


    @Nested
    @DisplayName("Clear Cart")
    class ClearTests {

        @Test
        @DisplayName("should clear all items")
        void shouldClearAllItems() {
            cart.addProduct(100L, 2);
            cart.addProduct(200L, 1);

            cart.clear();

            assertThat(cart.getItems()).isEmpty();
            assertThat(cart.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("should throw when cart is not active")
        void shouldThrowWhenNotActive() {
            cart.addProduct(100L, 1);
            cart.checkout(checkedOutStatus);

            assertThatThrownBy(() -> cart.clear())
                    .isInstanceOf(InvalidCartOperationException.class);
        }
    }

    @Nested
    @DisplayName("Checkout")
    class CheckoutTests {

        @Test
        @DisplayName("should checkout cart with items")
        void shouldCheckoutWithItems() {
            cart.addProduct(100L, 2);

            cart.checkout(checkedOutStatus);

            assertThat(cart.getStatus()).isEqualTo(checkedOutStatus);
            assertThat(cart.getCheckedOutAt()).isNotNull();
            assertThat(cart.isActive()).isFalse();
        }

        @Test
        @DisplayName("should throw when checking out empty cart")
        void shouldThrowWhenEmpty() {
            assertThatThrownBy(() -> cart.checkout(checkedOutStatus))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("Cannot checkout an empty cart");
        }

        @Test
        @DisplayName("should throw when cart is already checked out")
        void shouldThrowWhenAlreadyCheckedOut() {
            cart.addProduct(100L, 1);
            cart.checkout(checkedOutStatus);

            assertThatThrownBy(() -> cart.checkout(checkedOutStatus))
                    .isInstanceOf(InvalidCartOperationException.class);
        }
    }


    @Nested
    @DisplayName("Mark As Abandoned")
    class AbandonedTests {

        @Test
        @DisplayName("should mark cart as abandoned")
        void shouldMarkAsAbandoned() {
            cart.markAsAbandoned(abandonedStatus);

            assertThat(cart.getStatus()).isEqualTo(abandonedStatus);
            assertThat(cart.isActive()).isFalse();
        }

        @Test
        @DisplayName("should throw when cart is not active")
        void shouldThrowWhenNotActive() {
            cart.markAsAbandoned(abandonedStatus);

            assertThatThrownBy(() -> cart.markAsAbandoned(abandonedStatus))
                    .isInstanceOf(InvalidCartOperationException.class);
        }
    }

    @Nested
    @DisplayName("Utility Methods")
    class UtilityTests {

        @Test
        @DisplayName("should return total items count")
        void shouldReturnTotalItems() {
            cart.addProduct(100L, 3);
            cart.addProduct(200L, 2);

            int total = cart.getTotalItems();

            assertThat(total).isEqualTo(5);
        }

        @Test
        @DisplayName("should return 0 quantity for product not in cart")
        void shouldReturn0ForMissingProduct() {
            int qty = cart.getProductQuantity(999L);

            assertThat(qty).isEqualTo(0);
        }

        @Test
        @DisplayName("should report isEmpty correctly")
        void shouldReportIsEmpty() {
            assertThat(cart.isEmpty()).isTrue();

            cart.addProduct(100L, 1);
            assertThat(cart.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("should report isActive correctly")
        void shouldReportIsActive() {
            assertThat(cart.isActive()).isTrue();

            cart.markAsAbandoned(abandonedStatus);
            assertThat(cart.isActive()).isFalse();
        }

        @Test
        @DisplayName("should check containsProduct correctly")
        void shouldCheckContainsProduct() {
            cart.addProduct(100L, 1);

            assertThat(cart.containsProduct(100L)).isTrue();
            assertThat(cart.containsProduct(999L)).isFalse();
        }
    }
}


