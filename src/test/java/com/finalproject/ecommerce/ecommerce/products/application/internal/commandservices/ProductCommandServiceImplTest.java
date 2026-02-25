package com.finalproject.ecommerce.ecommerce.products.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.notifications.interfaces.acl.NotificationContextFacade;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.acl.OrdersContextFacade;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.CategoryNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.DuplicateCategoryAssignmentException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductInOrdersException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductNotFoundException;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.Category;
import com.finalproject.ecommerce.ecommerce.products.domain.model.entities.ProductPriceLog;
import com.finalproject.ecommerce.ecommerce.products.domain.model.valueobjects.PriceType;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.CategoryRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductCategoryRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductPriceLogRepository;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCommandServiceImpl")
class ProductCommandServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductCategoryRepository productCategoryRepository;
    @Mock private ProductPriceLogRepository productPriceLogRepository;
    @Mock private IamContextFacade iamContextFacade;
    @Mock private OrdersContextFacade ordersContextFacade;
    @Mock private NotificationContextFacade notificationContextFacade;

    @InjectMocks private ProductCommandServiceImpl service;

    private Product existingProduct;

    @BeforeEach
    void setUp() {
        var cmd = new CreateProductCommand("Test Product", "desc", new BigDecimal("50.00"), 100, List.of(1L), true);
        existingProduct = new Product(cmd, 1L);
    }

    @Nested
    @DisplayName("Create Product")
    class CreateProductTests {

        @Test
        @DisplayName("should create product successfully")
        void shouldCreate() {
            var cmd = new CreateProductCommand("New", "d", new BigDecimal("25.00"), 10, List.of(1L), true);
            var cat = new Category(); cat.setName("Electronics");
            when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
            when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

            service.handle(cmd);

            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("should throw when not authenticated")
        void shouldThrowNotAuth() {
            var cmd = new CreateProductCommand("P", "D", BigDecimal.TEN, 5, List.of(1L), true);
            when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not authenticated");
        }

        @Test
        @DisplayName("should throw when no categories")
        void shouldThrowNoCategories() {
            var cmd = new CreateProductCommand("P", "D", BigDecimal.TEN, 5, List.of(), true);
            when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.of(1L));

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("category is required");
        }

        @Test
        @DisplayName("should throw when category not found")
        void shouldThrowCategoryNotFound() {
            var cmd = new CreateProductCommand("P", "D", BigDecimal.TEN, 5, List.of(99L), true);
            when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(cmd))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Update Product")
    class UpdateProductTests {

        @Test
        @DisplayName("should update and return product")
        void shouldUpdate() {
            var cmd = new UpdateProductCommand(1L, "Updated", null, null, null);
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var result = service.handle(cmd);

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Updated");
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrowNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new UpdateProductCommand(99L, "X", null, null, null)))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("should log base price change")
        void shouldLogPriceChange() {
            var cmd = new UpdateProductCommand(1L, null, null, new BigDecimal("75.00"), null);
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            service.handle(cmd);

            var captor = ArgumentCaptor.forClass(ProductPriceLog.class);
            verify(productPriceLogRepository).save(captor.capture());
            assertThat(captor.getValue().getPriceType()).isEqualTo(PriceType.BASE_PRICE);
            assertThat(captor.getValue().getOldPrice()).isEqualByComparingTo("50.00");
            assertThat(captor.getValue().getNewPrice()).isEqualByComparingTo("75.00");
        }

        @Test
        @DisplayName("should not log when price unchanged")
        void shouldNotLogSamePrice() {
            var cmd = new UpdateProductCommand(1L, "Renamed", null, new BigDecimal("50.00"), null);
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            service.handle(cmd);

            verify(productPriceLogRepository, never()).save(any());
        }

        @Test
        @DisplayName("should not log when price is null")
        void shouldNotLogNullPrice() {
            var cmd = new UpdateProductCommand(1L, "Renamed", null, null, null);
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            service.handle(cmd);

            verify(productPriceLogRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Product")
    class DeleteProductTests {

        @Test
        @DisplayName("should hard delete when no orders")
        void shouldDelete() {
            when(productRepository.existsById(1L)).thenReturn(true);
            when(ordersContextFacade.productExistsInOrders(1L)).thenReturn(false);

            service.handle(new DeleteProductCommand(1L));

            verify(productRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when not found")
        void shouldThrowNotFound() {
            when(productRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.handle(new DeleteProductCommand(99L)))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when product in orders")
        void shouldThrowInOrders() {
            when(productRepository.existsById(1L)).thenReturn(true);
            when(ordersContextFacade.productExistsInOrders(1L)).thenReturn(true);

            assertThatThrownBy(() -> service.handle(new DeleteProductCommand(1L)))
                    .isInstanceOf(ProductInOrdersException.class);
        }
    }

    @Nested
    @DisplayName("Soft Delete")
    class SoftDeleteTests {

        @Test
        @DisplayName("should soft delete product")
        void shouldSoftDelete() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(ordersContextFacade.productExistsInOrders(1L)).thenReturn(false);
            when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var result = service.handle(new SoftDeleteProductCommand(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getIsDeleted()).isTrue();
            assertThat(result.get().isActive()).isFalse();
        }

        @Test
        @DisplayName("should throw when product in orders")
        void shouldThrowInOrders() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(ordersContextFacade.productExistsInOrders(1L)).thenReturn(true);

            assertThatThrownBy(() -> service.handle(new SoftDeleteProductCommand(1L)))
                    .isInstanceOf(ProductInOrdersException.class);
        }
    }

    @Nested
    @DisplayName("Assign Category")
    class AssignCategoryTests {

        @Test
        @DisplayName("should assign category")
        void shouldAssign() {
            var cat = new Category(); cat.setName("Books");
            when(productRepository.existsById(1L)).thenReturn(true);
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
            when(productCategoryRepository.existsByProduct_IdAndCategory_Id(1L, 2L)).thenReturn(false);
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var result = service.handle(new AssignCategoryToProductCommand(1L, 2L));

            assertThat(result).isPresent();
            verify(productRepository).save(any());
        }

        @Test
        @DisplayName("should throw when product not found")
        void shouldThrowProductNotFound() {
            when(productRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> service.handle(new AssignCategoryToProductCommand(99L, 1L)))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when category not found")
        void shouldThrowCategoryNotFound() {
            when(productRepository.existsById(1L)).thenReturn(true);
            when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new AssignCategoryToProductCommand(1L, 99L)))
                    .isInstanceOf(CategoryNotFoundException.class);
        }

        @Test
        @DisplayName("should throw when already assigned")
        void shouldThrowDuplicate() {
            var cat = new Category(); cat.setName("Books");
            when(productRepository.existsById(1L)).thenReturn(true);
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
            when(productCategoryRepository.existsByProduct_IdAndCategory_Id(1L, 2L)).thenReturn(true);

            assertThatThrownBy(() -> service.handle(new AssignCategoryToProductCommand(1L, 2L)))
                    .isInstanceOf(DuplicateCategoryAssignmentException.class);
        }
    }

    @Nested
    @DisplayName("Toggle Like")
    class ToggleLikeTests {

        @Test
        @DisplayName("should like with provided userId")
        void shouldLikeWithUserId() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            boolean result = service.handle(new ToggleProductLikeCommand(10L, 1L));

            assertThat(result).isTrue();
            verify(iamContextFacade).validateUserCanAccessResource(10L);
        }

        @Test
        @DisplayName("should use current user when userId is null")
        void shouldUseCurrentUser() {
            when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.of(5L));
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            boolean result = service.handle(new ToggleProductLikeCommand(null, 1L));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should throw when product not found")
        void shouldThrowNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new ToggleProductLikeCommand(10L, 99L)))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Stock Operations")
    class StockTests {

        @Test
        @DisplayName("should decrease stock")
        void shouldDecrease() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var result = service.handle(new DecreaseProductStockCommand(1L, 30));

            assertThat(result).isPresent();
            assertThat(result.get().getStock()).isEqualTo(70);
        }

        @Test
        @DisplayName("should increase stock")
        void shouldIncrease() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var result = service.handle(new IncreaseProductStockCommand(1L, 50));

            assertThat(result).isPresent();
            assertThat(result.get().getStock()).isEqualTo(150);
        }

        @Test
        @DisplayName("should throw decrease on missing product")
        void shouldThrowDecreaseNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new DecreaseProductStockCommand(99L, 1)))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("should throw increase on missing product")
        void shouldThrowIncreaseNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new IncreaseProductStockCommand(99L, 1)))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Activate / Deactivate")
    class ActivateDeactivateTests {

        @Test
        @DisplayName("should activate")
        void shouldActivate() {
            existingProduct.deactivate();
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var result = service.handle(new ActivateProductCommand(1L));

            assertThat(result).isPresent();
            assertThat(result.get().isActive()).isTrue();
        }

        @Test
        @DisplayName("should deactivate")
        void shouldDeactivate() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var result = service.handle(new DeactivateProductCommand(1L));

            assertThat(result).isPresent();
            assertThat(result.get().isActive()).isFalse();
        }

        @Test
        @DisplayName("should throw activate not found")
        void shouldThrowActivateNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new ActivateProductCommand(99L)))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        @DisplayName("should throw deactivate not found")
        void shouldThrowDeactivateNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new DeactivateProductCommand(99L)))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Set Sale Price")
    class SetSalePriceTests {

        private Instant validExpire;

        @BeforeEach
        void init() { validExpire = Instant.now().plus(Duration.ofDays(7)); }

        @Test
        @DisplayName("should set sale price and log it")
        void shouldSetAndLog() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var result = service.handle(new SetProductSalePriceCommand(1L, new BigDecimal("30.00"), validExpire));

            assertThat(result).isPresent();
            assertThat(result.get().getSalePrice()).isEqualByComparingTo("30.00");
            var captor = ArgumentCaptor.forClass(ProductPriceLog.class);
            verify(productPriceLogRepository).save(captor.capture());
            assertThat(captor.getValue().getPriceType()).isEqualTo(PriceType.SALE_PRICE);
        }

        @Test
        @DisplayName("should notify liked users")
        void shouldNotify() {
            existingProduct.toggleLike(10L);
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(iamContextFacade.getUserEmails(List.of(10L))).thenReturn(Map.of(10L, "u@t.com"));

            service.handle(new SetProductSalePriceCommand(1L, new BigDecimal("30.00"), validExpire));

            verify(notificationContextFacade).sendDiscountAlertBatch(
                    anySet(), eq("Test Product"), anyString(), eq("30.00"), anyString(), anyString());
        }

        @Test
        @DisplayName("should not notify when no likes")
        void shouldNotNotifyNoLikes() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
            when(productRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            service.handle(new SetProductSalePriceCommand(1L, new BigDecimal("30.00"), validExpire));

            verify(notificationContextFacade, never()).sendDiscountAlertBatch(
                    anySet(), anyString(), anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("should throw when product not found")
        void shouldThrowNotFound() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new SetProductSalePriceCommand(99L, BigDecimal.TEN, validExpire)))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }
}

