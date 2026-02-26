package com.finalproject.ecommerce.ecommerce.carts.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.carts.domain.exceptions.CartNotFoundException;
import com.finalproject.ecommerce.ecommerce.carts.domain.exceptions.InvalidCartOperationException;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.entities.CartStatus;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.valueobjects.CartStatuses;
import com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories.CartRepository;
import com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories.CartStatusRepository;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.products.interfaces.acl.ProductContextFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartCommandServiceImpl")
class CartCommandServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartStatusRepository cartStatusRepository;
    @Mock private IamContextFacade iamContextFacade;
    @Mock private ProductContextFacade productContextFacade;

    @InjectMocks private CartCommandServiceImpl service;

    private CartStatus activeStatus;
    private CartStatus checkedOutStatus;

    @BeforeEach
    void setUp() {
        activeStatus = new CartStatus(CartStatuses.ACTIVE, "Active");
        checkedOutStatus = new CartStatus(CartStatuses.CHECKED_OUT, "Checked out");
    }

    @Nested
    @DisplayName("Add Product To Cart")
    class AddProductToCartTests {

        @Test
        @DisplayName("should add product to existing cart with provided userId")
        void shouldAddProductToExistingCart() {
            var cart = new Cart(1L, activeStatus);
            var cmd = new AddProductToCartCommand(1L, 100L, 2);
            when(iamContextFacade.userExists(1L)).thenReturn(true);
            when(productContextFacade.getProductStock(100L)).thenReturn(10);
            when(productContextFacade.isProductDeleted(100L)).thenReturn(false);
            when(productContextFacade.isProductActive(100L)).thenReturn(true);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

            Cart result = service.handle(cmd);

            assertThat(result.containsProduct(100L)).isTrue();
            assertThat(result.getProductQuantity(100L)).isEqualTo(2);
            verify(cartRepository).save(any(Cart.class));
        }

        @Test
        @DisplayName("should create new cart when none exists")
        void shouldCreateNewCartWhenNoneExists() {
            var cmd = new AddProductToCartCommand(1L, 100L, 1);
            var newCart = new Cart(1L, activeStatus);
            when(iamContextFacade.userExists(1L)).thenReturn(true);
            when(productContextFacade.getProductStock(100L)).thenReturn(10);
            when(productContextFacade.isProductDeleted(100L)).thenReturn(false);
            when(productContextFacade.isProductActive(100L)).thenReturn(true);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenReturn(newCart).thenAnswer(i -> i.getArgument(0));

            Cart result = service.handle(cmd);

            assertThat(result).isNotNull();
            verify(cartRepository, atLeast(2)).save(any(Cart.class));
        }

        @Test
        @DisplayName("should use current userId when userId is null")
        void shouldUseCurrentUserWhenNull() {
            var cmd = new AddProductToCartCommand(null, 100L, 1);
            var cart = new Cart(5L, activeStatus);
            when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.of(5L));
            when(iamContextFacade.userExists(5L)).thenReturn(true);
            when(productContextFacade.getProductStock(100L)).thenReturn(10);
            when(productContextFacade.isProductDeleted(100L)).thenReturn(false);
            when(productContextFacade.isProductActive(100L)).thenReturn(true);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(5L, activeStatus)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

            Cart result = service.handle(cmd);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("should throw when not authenticated and userId is null")
        void shouldThrowWhenNotAuthenticated() {
            var cmd = new AddProductToCartCommand(null, 100L, 1);
            when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not authenticated");
        }

        @Test
        @DisplayName("should throw when user does not exist")
        void shouldThrowWhenUserNotExists() {
            var cmd = new AddProductToCartCommand(999L, 100L, 1);
            when(iamContextFacade.userExists(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("does not exist");
        }

        @Test
        @DisplayName("should throw when product is out of stock")
        void shouldThrowWhenOutOfStock() {
            var cmd = new AddProductToCartCommand(1L, 100L, 1);
            when(iamContextFacade.userExists(1L)).thenReturn(true);
            when(productContextFacade.getProductStock(100L)).thenReturn(0);

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("not available or out of stock");
        }

        @Test
        @DisplayName("should throw when product stock is null")
        void shouldThrowWhenStockNull() {
            var cmd = new AddProductToCartCommand(1L, 100L, 1);
            when(iamContextFacade.userExists(1L)).thenReturn(true);
            when(productContextFacade.getProductStock(100L)).thenReturn(null);

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("not available or out of stock");
        }

        @Test
        @DisplayName("should throw when product is deleted")
        void shouldThrowWhenProductDeleted() {
            var cmd = new AddProductToCartCommand(1L, 100L, 1);
            when(iamContextFacade.userExists(1L)).thenReturn(true);
            when(productContextFacade.getProductStock(100L)).thenReturn(10);
            when(productContextFacade.isProductDeleted(100L)).thenReturn(true);

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("not available");
        }

        @Test
        @DisplayName("should throw when product is inactive")
        void shouldThrowWhenProductInactive() {
            var cmd = new AddProductToCartCommand(1L, 100L, 1);
            when(iamContextFacade.userExists(1L)).thenReturn(true);
            when(productContextFacade.getProductStock(100L)).thenReturn(10);
            when(productContextFacade.isProductDeleted(100L)).thenReturn(false);
            when(productContextFacade.isProductActive(100L)).thenReturn(false);

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("not available");
        }

        @Test
        @DisplayName("should throw when total quantity exceeds stock and cart already at max")
        void shouldThrowWhenExceedsStockAtMax() {
            var cart = new Cart(1L, activeStatus);
            cart.addProduct(100L, 5);
            var cmd = new AddProductToCartCommand(1L, 100L, 1);
            when(iamContextFacade.userExists(1L)).thenReturn(true);
            when(productContextFacade.getProductStock(100L)).thenReturn(5);
            when(productContextFacade.isProductDeleted(100L)).thenReturn(false);
            when(productContextFacade.isProductActive(100L)).thenReturn(true);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.of(cart));

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("maximum available quantity");
        }

        @Test
        @DisplayName("should throw when adding quantity exceeds remaining stock")
        void shouldThrowWhenExceedsRemainingStock() {
            var cart = new Cart(1L, activeStatus);
            cart.addProduct(100L, 3);
            var cmd = new AddProductToCartCommand(1L, 100L, 4);
            when(iamContextFacade.userExists(1L)).thenReturn(true);
            when(productContextFacade.getProductStock(100L)).thenReturn(5);
            when(productContextFacade.isProductDeleted(100L)).thenReturn(false);
            when(productContextFacade.isProductActive(100L)).thenReturn(true);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.of(cart));

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("You can add up to");
        }
    }


    @Nested
    @DisplayName("Update Cart Item Quantity")
    class UpdateCartItemQuantityTests {

        @Test
        @DisplayName("should update product quantity in cart")
        void shouldUpdateQuantity() {
            var cart = new Cart(1L, activeStatus);
            cart.addProduct(100L, 2);
            var cmd = new UpdateCartItemQuantityCommand(1L, 100L, 5);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.of(cart));
            when(productContextFacade.isProductDeleted(100L)).thenReturn(false);
            when(productContextFacade.isProductActive(100L)).thenReturn(true);
            when(productContextFacade.getProductStock(100L)).thenReturn(10);
            when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

            Cart result = service.handle(cmd);

            assertThat(result.getProductQuantity(100L)).isEqualTo(5);
        }

        @Test
        @DisplayName("should throw when cart not found")
        void shouldThrowWhenCartNotFound() {
            var cmd = new UpdateCartItemQuantityCommand(1L, 100L, 5);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(CartNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when product is deleted")
        void shouldThrowWhenProductDeleted() {
            var cart = new Cart(1L, activeStatus);
            cart.addProduct(100L, 2);
            var cmd = new UpdateCartItemQuantityCommand(1L, 100L, 3);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.of(cart));
            when(productContextFacade.isProductDeleted(100L)).thenReturn(true);

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("not available");
        }

        @Test
        @DisplayName("should throw when product is not active")
        void shouldThrowWhenProductNotActive() {
            var cart = new Cart(1L, activeStatus);
            cart.addProduct(100L, 2);
            var cmd = new UpdateCartItemQuantityCommand(1L, 100L, 3);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.of(cart));
            when(productContextFacade.isProductDeleted(100L)).thenReturn(false);
            when(productContextFacade.isProductActive(100L)).thenReturn(false);

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("not available");
        }

        @Test
        @DisplayName("should throw when quantity exceeds stock")
        void shouldThrowWhenExceedsStock() {
            var cart = new Cart(1L, activeStatus);
            cart.addProduct(100L, 2);
            var cmd = new UpdateCartItemQuantityCommand(1L, 100L, 20);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.of(cart));
            when(productContextFacade.isProductDeleted(100L)).thenReturn(false);
            when(productContextFacade.isProductActive(100L)).thenReturn(true);
            when(productContextFacade.getProductStock(100L)).thenReturn(10);

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("Cannot set quantity");
        }

        @Test
        @DisplayName("should throw when product out of stock")
        void shouldThrowWhenOutOfStock() {
            var cart = new Cart(1L, activeStatus);
            cart.addProduct(100L, 2);
            var cmd = new UpdateCartItemQuantityCommand(1L, 100L, 3);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.of(cart));
            when(productContextFacade.isProductDeleted(100L)).thenReturn(false);
            when(productContextFacade.isProductActive(100L)).thenReturn(true);
            when(productContextFacade.getProductStock(100L)).thenReturn(0);

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(InvalidCartOperationException.class)
                    .hasMessageContaining("out of stock");
        }
    }

    @Nested
    @DisplayName("Remove Product From Cart")
    class RemoveProductTests {

        @Test
        @DisplayName("should remove product from cart")
        void shouldRemoveProduct() {
            var cart = new Cart(1L, activeStatus);
            cart.addProduct(100L, 2);
            var cmd = new RemoveProductFromCartCommand(1L, 100L);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

            Cart result = service.handle(cmd);

            assertThat(result.containsProduct(100L)).isFalse();
        }

        @Test
        @DisplayName("should throw when cart not found")
        void shouldThrowWhenCartNotFound() {
            var cmd = new RemoveProductFromCartCommand(1L, 100L);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(CartNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Remove Cart Item")
    class RemoveCartItemTests {

        @Test
        @DisplayName("should throw when cart not found")
        void shouldThrowWhenCartNotFound() {
            var cmd = new RemoveCartItemCommand(1L, 10L);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(CartNotFoundException.class);
        }
    }


    @Nested
    @DisplayName("Clear Cart")
    class ClearCartTests {

        @Test
        @DisplayName("should clear cart items")
        void shouldClearCart() {
            var cart = new Cart(1L, activeStatus);
            cart.addProduct(100L, 2);
            cart.addProduct(200L, 3);
            var cmd = new ClearCartCommand(1L);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.of(cart));
            when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

            Cart result = service.handle(cmd);

            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("should throw when cart not found")
        void shouldThrowWhenCartNotFound() {
            var cmd = new ClearCartCommand(1L);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(CartNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Checkout Cart")
    class CheckoutCartTests {

        @Test
        @DisplayName("should checkout cart successfully")
        void shouldCheckout() {
            var cart = new Cart(1L, activeStatus);
            cart.addProduct(100L, 2);
            var cmd = new CheckoutCartCommand(1L);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.of(cart));
            when(cartStatusRepository.findByName(CartStatuses.CHECKED_OUT.name())).thenReturn(Optional.of(checkedOutStatus));
            when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

            Cart result = service.handle(cmd);

            assertThat(result.isActive()).isFalse();
            assertThat(result.getCheckedOutAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw when cart not found")
        void shouldThrowWhenCartNotFound() {
            var cmd = new CheckoutCartCommand(1L);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(CartNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when checked out status not found in DB")
        void shouldThrowWhenCheckedOutStatusNotFound() {
            var cart = new Cart(1L, activeStatus);
            cart.addProduct(100L, 1);
            var cmd = new CheckoutCartCommand(1L);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.of(cart));
            when(cartStatusRepository.findByName(CartStatuses.CHECKED_OUT.name())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Checked out cart status not found");
        }
    }

    @Nested
    @DisplayName("Seed Cart Statuses")
    class SeedCartStatusTests {

        @Test
        @DisplayName("should seed statuses that do not exist")
        void shouldSeedMissingStatuses() {
            when(cartStatusRepository.existsByName(anyString())).thenReturn(false);

            service.handle(new SeedCartStatusCommand());

            verify(cartStatusRepository, times(CartStatuses.values().length)).save(any(CartStatus.class));
        }

        @Test
        @DisplayName("should not seed statuses that already exist")
        void shouldNotSeedExistingStatuses() {
            when(cartStatusRepository.existsByName(anyString())).thenReturn(true);

            service.handle(new SeedCartStatusCommand());

            verify(cartStatusRepository, never()).save(any(CartStatus.class));
        }
    }
}

