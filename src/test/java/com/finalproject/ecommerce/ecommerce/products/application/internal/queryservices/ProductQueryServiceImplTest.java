package com.finalproject.ecommerce.ecommerce.products.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.products.application.dto.ProductPageResponse;
import com.finalproject.ecommerce.ecommerce.products.domain.model.aggregates.Product;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.CreateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.queries.*;
import com.finalproject.ecommerce.ecommerce.products.infrastructure.persistence.jpa.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProductQueryServiceImpl")
class ProductQueryServiceImplTest {

    @Mock private ProductRepository productRepository;

    @InjectMocks private ProductQueryServiceImpl service;

    private Product sampleProduct;
    private Product deletedProduct;

    @BeforeEach
    void setUp() {
        var cmd = new CreateProductCommand("Sample", "desc", new BigDecimal("10.00"), 50, List.of(1L), true);
        sampleProduct = new Product(cmd, 1L);

        var cmd2 = new CreateProductCommand("Deleted", "d", new BigDecimal("5.00"), 0, List.of(1L), false);
        deletedProduct = new Product(cmd2, 1L);
        deletedProduct.softDelete();
    }

    @Nested
    @DisplayName("Get Product By Id")
    class GetProductByIdTests {

        @Test
        @DisplayName("should return product when found")
        void shouldReturnProduct() {
            when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

            var result = service.handle(new GetProductByIdQuery(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Sample");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmpty() {
            when(productRepository.findById(99L)).thenReturn(Optional.empty());

            var result = service.handle(new GetProductByIdQuery(99L));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Active Products")
    class GetActiveProductsTests {

        @Test
        @DisplayName("should return only active non-deleted products")
        void shouldReturnActiveProducts() {
            when(productRepository.findByIsDeletedAndIsActive(false, true))
                    .thenReturn(List.of(sampleProduct));

            var result = service.handle(new GetActiveProductsQuery());

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getName()).isEqualTo("Sample");
        }

        @Test
        @DisplayName("should return empty list when no active products")
        void shouldReturnEmptyList() {
            when(productRepository.findByIsDeletedAndIsActive(false, true))
                    .thenReturn(List.of());

            var result = service.handle(new GetActiveProductsQuery());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Products By Ids")
    class GetProductsByIdsTests {

        @Test
        @DisplayName("should return non-deleted products for given IDs")
        void shouldReturnNonDeletedProducts() {
            when(productRepository.findAllById(List.of(1L, 2L)))
                    .thenReturn(List.of(sampleProduct, deletedProduct));

            var result = service.handle(new GetProductsByIdsQuery(List.of(1L, 2L)));

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getName()).isEqualTo("Sample");
        }

        @Test
        @DisplayName("should return empty list when IDs are null")
        void shouldReturnEmptyForNull() {
            var result = service.handle(new GetProductsByIdsQuery(null));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty list when IDs are empty")
        void shouldReturnEmptyForEmptyList() {
            var result = service.handle(new GetProductsByIdsQuery(List.of()));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Products With Pagination")
    class PaginationTests {

        @Test
        @DisplayName("should apply isActive=true for non-manager users")
        void shouldFilterActiveForClient() {
            var page = new PageImpl<>(List.of(sampleProduct));
            when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(productRepository.findDistinctByIdIn(anyList())).thenReturn(List.of(sampleProduct));
            when(productRepository.findByIdIn(anyList())).thenReturn(List.of(sampleProduct));

            var query = new GetProductsWithPaginationQuery(null, null, 0, 20, "id", "asc");
            ProductPageResponse result = service.handle(query, false);

            assertThat(result.products()).hasSize(1);
            assertThat(result.pageMetadata().currentPage()).isEqualTo(0);
            assertThat(result.pageMetadata().pageSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("should allow manager to see all products including inactive")
        void shouldAllowManagerInactive() {
            var page = new PageImpl<>(List.of(sampleProduct));
            when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(productRepository.findDistinctByIdIn(anyList())).thenReturn(List.of(sampleProduct));
            when(productRepository.findByIdIn(anyList())).thenReturn(List.of(sampleProduct));

            var query = new GetProductsWithPaginationQuery(null, false, 0, 20, "id", "asc");
            ProductPageResponse result = service.handle(query, true);

            assertThat(result.products()).hasSize(1);
        }

        @Test
        @DisplayName("should return correct page metadata")
        void shouldReturnCorrectMetadata() {
            var page = new PageImpl<>(List.of(sampleProduct), org.springframework.data.domain.PageRequest.of(0, 20), 1);
            when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(productRepository.findDistinctByIdIn(anyList())).thenReturn(List.of(sampleProduct));
            when(productRepository.findByIdIn(anyList())).thenReturn(List.of(sampleProduct));

            var query = new GetProductsWithPaginationQuery(null, null, 0, 20, "id", "asc");
            ProductPageResponse result = service.handle(query, false);

            assertThat(result.pageMetadata().totalElements()).isEqualTo(1);
            assertThat(result.pageMetadata().totalPages()).isEqualTo(1);
            assertThat(result.pageMetadata().hasNext()).isFalse();
            assertThat(result.pageMetadata().hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("should return empty products list when no products match")
        void shouldReturnEmptyPage() {
            var emptyPage = new PageImpl<Product>(List.of());
            when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

            var query = new GetProductsWithPaginationQuery(null, null, 0, 20, "id", "asc");
            ProductPageResponse result = service.handle(query, false);

            assertThat(result.products()).isEmpty();
            assertThat(result.pageMetadata().totalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("should support desc sort direction")
        void shouldSupportDescSort() {
            var page = new PageImpl<>(List.of(sampleProduct));
            when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            when(productRepository.findDistinctByIdIn(anyList())).thenReturn(List.of(sampleProduct));
            when(productRepository.findByIdIn(anyList())).thenReturn(List.of(sampleProduct));

            var query = new GetProductsWithPaginationQuery(null, null, 0, 20, "price", "desc");
            ProductPageResponse result = service.handle(query, false);

            assertThat(result.products()).hasSize(1);
        }
    }
}

