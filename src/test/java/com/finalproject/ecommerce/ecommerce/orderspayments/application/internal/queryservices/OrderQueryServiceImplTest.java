package com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.aggregates.Order;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.OrderStatus;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetAllOrdersWithPaginationQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetOrderByIdQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetUserOrdersWithPaginationQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.valueobjects.OrderStatuses;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderQueryServiceImpl")
class OrderQueryServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private IamContextFacade iamContextFacade;

    @InjectMocks private OrderQueryServiceImpl service;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        OrderStatus pendingStatus = new OrderStatus(OrderStatuses.PENDING, "Pending");
        sampleOrder = new Order(1L, 10L, 100L, pendingStatus);
    }

    @Nested
    @DisplayName("Get Order By Id")
    class GetOrderByIdTests {

        @Test
        @DisplayName("should return order when found")
        void shouldReturnOrder() {
            when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

            var result = service.handle(new GetOrderByIdQuery(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo(1L);
            verify(iamContextFacade).validateManagerOrUserCanAccessResource(1L);
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmpty() {
            when(orderRepository.findById(99L)).thenReturn(Optional.empty());

            var result = service.handle(new GetOrderByIdQuery(99L));

            assertThat(result).isEmpty();
            verify(iamContextFacade, never()).validateManagerOrUserCanAccessResource(anyLong());
        }
    }

    @Nested
    @DisplayName("Get All Orders With Pagination")
    class GetAllOrdersTests {

        @Test
        @DisplayName("should return paginated orders with ascending sort")
        void shouldReturnPaginatedOrdersAsc() {
            Page<Order> page = new PageImpl<>(List.of(sampleOrder));
            when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            var query = new GetAllOrdersWithPaginationQuery(0, 20, "id", "asc", null, null, null, null, null);

            Page<Order> result = service.handle(query);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should return paginated orders with descending sort")
        void shouldReturnPaginatedOrdersDesc() {
            Page<Order> page = new PageImpl<>(List.of(sampleOrder));
            when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
            var query = new GetAllOrdersWithPaginationQuery(0, 20, "id", "desc", null, null, null, null, null);

            Page<Order> result = service.handle(query);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should return empty page when no orders match")
        void shouldReturnEmptyPage() {
            Page<Order> emptyPage = new PageImpl<>(List.of());
            when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);
            var query = new GetAllOrdersWithPaginationQuery(0, 20, "id", "asc", null, null, null, null, null);

            Page<Order> result = service.handle(query);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get User Orders With Pagination")
    class GetUserOrdersTests {

        @Test
        @DisplayName("should return user orders with ascending sort")
        void shouldReturnUserOrdersAsc() {
            Page<Order> page = new PageImpl<>(List.of(sampleOrder));
            when(orderRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);
            var query = new GetUserOrdersWithPaginationQuery(1L, 0, 20, "id", "asc");

            Page<Order> result = service.handle(query);

            assertThat(result.getContent()).hasSize(1);
            verify(iamContextFacade).validateManagerOrUserCanAccessResource(1L);
        }

        @Test
        @DisplayName("should return user orders with descending sort")
        void shouldReturnUserOrdersDesc() {
            Page<Order> page = new PageImpl<>(List.of(sampleOrder));
            when(orderRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);
            var query = new GetUserOrdersWithPaginationQuery(1L, 0, 20, "id", "desc");

            Page<Order> result = service.handle(query);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("should return empty page when user has no orders")
        void shouldReturnEmptyPageForUser() {
            Page<Order> emptyPage = new PageImpl<>(List.of());
            when(orderRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(emptyPage);
            var query = new GetUserOrdersWithPaginationQuery(1L, 0, 20, "id", "asc");

            Page<Order> result = service.handle(query);

            assertThat(result.getContent()).isEmpty();
        }
    }
}

