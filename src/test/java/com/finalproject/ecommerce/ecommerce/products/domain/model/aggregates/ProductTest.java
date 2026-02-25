package com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates;

import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductAlreadyLikedException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductNotLikedException;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateProductCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Product Aggregate")
class ProductTest {

    private Product product;

    @BeforeEach
    void setUp() {
        var command = new CreateProductCommand(
                "Test Product", "A test description",
                new BigDecimal("29.99"), 100,
                List.of(1L), true
        );
        product = new Product(command, 1L);
    }


    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create product from command with correct fields")
        void shouldCreateFromCommand() {
            assertThat(product.getName()).isEqualTo("Test Product");
            assertThat(product.getDescription()).isEqualTo("A test description");
            assertThat(product.getPrice()).isEqualByComparingTo("29.99");
            assertThat(product.getStock()).isEqualTo(100);
            assertThat(product.getIsActive()).isTrue();
            assertThat(product.getIsDeleted()).isFalse();
            assertThat(product.getCreatedByUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should create inactive product when isActive is false")
        void shouldCreateInactiveProduct() {
            var cmd = new CreateProductCommand("P", "D", BigDecimal.TEN, 5, List.of(1L), false);

            var p = new Product(cmd, 2L);

            assertThat(p.getIsActive()).isFalse();
        }
    }


    @Nested
    @DisplayName("Update Product Info")
    class UpdateInfo {

        @Test
        @DisplayName("should update name when non-blank")
        void shouldUpdateName() {
            product.updateProductInfo("New Name", null, null, null);

            assertThat(product.getName()).isEqualTo("New Name");
        }

        @Test
        @DisplayName("should not update name when blank")
        void shouldNotUpdateBlankName() {
            product.updateProductInfo("  ", null, null, null);

            assertThat(product.getName()).isEqualTo("Test Product");
        }

        @Test
        @DisplayName("should update description")
        void shouldUpdateDescription() {
            product.updateProductInfo(null, "New desc", null, null);

            assertThat(product.getDescription()).isEqualTo("New desc");
        }

        @Test
        @DisplayName("should update price when >= 1")
        void shouldUpdatePrice() {
            product.updateProductInfo(null, null, new BigDecimal("50.00"), null);

            assertThat(product.getPrice()).isEqualByComparingTo("50.00");
        }

        @Test
        @DisplayName("should not update price when less than 1")
        void shouldNotUpdatePriceBelowOne() {
            product.updateProductInfo(null, null, new BigDecimal("0.50"), null);

            assertThat(product.getPrice()).isEqualByComparingTo("29.99");
        }

        @Test
        @DisplayName("should update stock when >= 0")
        void shouldUpdateStock() {
            product.updateProductInfo(null, null, null, 200);

            assertThat(product.getStock()).isEqualTo(200);
        }

        @Test
        @DisplayName("should not update stock when negative")
        void shouldNotUpdateNegativeStock() {
            product.updateProductInfo(null, null, null, -5);

            assertThat(product.getStock()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("should activate product with stock")
        void shouldActivate() {
            product.deactivate();

            product.activate();

            assertThat(product.isActive()).isTrue();
        }

        @Test
        @DisplayName("should throw when activating product with no stock")
        void shouldThrowWhenActivatingNoStock() {
            product.updateProductInfo(null, null, null, 0);
            product.deactivate();

            assertThatThrownBy(() -> product.activate())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no stock");
        }

        @Test
        @DisplayName("should deactivate product")
        void shouldDeactivate() {
            product.deactivate();

            assertThat(product.isActive()).isFalse();
        }

        @Test
        @DisplayName("should soft delete product setting isDeleted true and isActive false")
        void shouldSoftDelete() {
            product.softDelete();

            assertThat(product.getIsDeleted()).isTrue();
            assertThat(product.isActive()).isFalse();
        }

        @Test
        @DisplayName("should restore soft-deleted product")
        void shouldRestore() {
            product.softDelete();

            product.restore();

            assertThat(product.getIsDeleted()).isFalse();
        }
    }


    @Nested
    @DisplayName("Stock Management")
    class StockManagement {

        @Test
        @DisplayName("should decrease stock")
        void shouldDecreaseStock() {
            product.decreaseStock(30);

            assertThat(product.getStock()).isEqualTo(70);
        }

        @Test
        @DisplayName("should throw when decreasing more than available stock")
        void shouldThrowWhenInsufficientStock() {
            assertThatThrownBy(() -> product.decreaseStock(101))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("should throw when decreasing by zero or negative")
        void shouldThrowWhenDecreasingByZero() {
            assertThatThrownBy(() -> product.decreaseStock(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("should increase stock")
        void shouldIncreaseStock() {
            product.increaseStock(50);

            assertThat(product.getStock()).isEqualTo(150);
        }

        @Test
        @DisplayName("should throw when increasing by zero or negative")
        void shouldThrowWhenIncreasingByZero() {
            assertThatThrownBy(() -> product.increaseStock(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }
    }


    @Nested
    @DisplayName("Like System")
    class LikeSystem {

        @Test
        @DisplayName("should add like and return true on toggleLike for new user")
        void shouldToggleLikeOn() {
            boolean result = product.toggleLike(10L);

            assertThat(result).isTrue();
            assertThat(product.getLikesCount()).isEqualTo(1);
            assertThat(product.isLikedByUser(10L)).isTrue();
        }

        @Test
        @DisplayName("should remove like and return false on second toggleLike")
        void shouldToggleLikeOff() {
            product.toggleLike(10L);

            boolean result = product.toggleLike(10L);

            assertThat(result).isFalse();
            assertThat(product.getLikesCount()).isEqualTo(0);
            assertThat(product.isLikedByUser(10L)).isFalse();
        }

        @Test
        @DisplayName("should re-activate like on third toggle")
        void shouldReactivateLike() {
            product.toggleLike(10L);
            product.toggleLike(10L);

            boolean result = product.toggleLike(10L);

            assertThat(result).isTrue();
            assertThat(product.getLikesCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should throw when adding duplicate like via addLike")
        void shouldThrowOnDuplicateLike() {
            product.addLike(10L);

            assertThatThrownBy(() -> product.addLike(10L))
                    .isInstanceOf(ProductAlreadyLikedException.class);
        }

        @Test
        @DisplayName("should throw when removing like that does not exist")
        void shouldThrowOnRemoveNonExistentLike() {
            assertThatThrownBy(() -> product.removeLike(99L))
                    .isInstanceOf(ProductNotLikedException.class);
        }

        @Test
        @DisplayName("should return liked user IDs")
        void shouldReturnLikedUserIds() {
            product.toggleLike(10L);
            product.toggleLike(20L);

            var likedUserIds = product.getLikedByUserIds();

            assertThat(likedUserIds).containsExactlyInAnyOrder(10L, 20L);
        }

        @Test
        @DisplayName("should not include deactivated likes in count or user IDs")
        void shouldExcludeDeactivatedLikes() {
            product.toggleLike(10L);
            product.toggleLike(20L);
            product.toggleLike(10L);

            var likesCount = product.getLikesCount();
            var likedUserIds = product.getLikedByUserIds();

            assertThat(likesCount).isEqualTo(1);
            assertThat(likedUserIds).containsExactly(20L);
        }
    }

    @Nested
    @DisplayName("Sale Price")
    class SalePriceTests {

        private Instant validExpireDate;

        @BeforeEach
        void setUp() {
            validExpireDate = Instant.now().plus(Duration.ofDays(7));
        }

        @Test
        @DisplayName("should set sale price with valid values")
        void shouldSetSalePrice() {
            product.setSalePrice(new BigDecimal("19.99"), validExpireDate);

            assertThat(product.getSalePrice()).isEqualByComparingTo("19.99");
            assertThat(product.getSalePriceExpireDate()).isEqualTo(validExpireDate);
        }

        @Test
        @DisplayName("should report active sale price")
        void shouldHaveActiveSalePrice() {
            product.setSalePrice(new BigDecimal("19.99"), validExpireDate);

            boolean result = product.hasActiveSalePrice();

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return sale price as effective price when active")
        void shouldReturnSalePriceAsEffective() {
            product.setSalePrice(new BigDecimal("19.99"), validExpireDate);

            var effectivePrice = product.getEffectivePrice();

            assertThat(effectivePrice).isEqualByComparingTo("19.99");
        }

        @Test
        @DisplayName("should return base price as effective when no sale price")
        void shouldReturnBasePriceWhenNoSale() {
            var effectivePrice = product.getEffectivePrice();

            assertThat(effectivePrice).isEqualByComparingTo("29.99");
        }

        @Test
        @DisplayName("should not have active sale price when no sale price set")
        void shouldNotHaveActiveSalePriceWhenNone() {
            boolean hasActive = product.hasActiveSalePrice();
            var effectivePrice = product.getEffectivePrice();

            assertThat(hasActive).isFalse();
            assertThat(effectivePrice).isEqualByComparingTo("29.99");
        }

        @Test
        @DisplayName("should throw when sale price >= base price")
        void shouldThrowWhenSalePriceNotLessThanBase() {
            assertThatThrownBy(() -> product.setSalePrice(new BigDecimal("30.00"), validExpireDate))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("less than the base price");
        }

        @Test
        @DisplayName("should throw when sale price has no expire date")
        void shouldThrowWhenNoExpireDate() {
            assertThatThrownBy(() -> product.setSalePrice(new BigDecimal("10.00"), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("expire date is required");
        }

        @Test
        @DisplayName("should throw when expire date is less than 24 hours from now")
        void shouldThrowWhenExpireDateTooSoon() {
            Instant tooSoon = Instant.now().plus(Duration.ofHours(1));

            assertThatThrownBy(() -> product.setSalePrice(new BigDecimal("10.00"), tooSoon))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("at least 24 hours");
        }

        @Test
        @DisplayName("should clear sale price when set to null")
        void shouldClearSalePrice() {
            product.setSalePrice(new BigDecimal("19.99"), validExpireDate);

            product.setSalePrice(null, null);

            assertThat(product.getSalePrice()).isNull();
            assertThat(product.getSalePriceExpireDate()).isNull();
            assertThat(product.hasActiveSalePrice()).isFalse();
        }
    }
}


