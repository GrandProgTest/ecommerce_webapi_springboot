package com.finalproject.ecommerce.ecommerce.carts.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.entities.CartStatus;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCurrentUserCartQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.valueobjects.CartStatuses;
import com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories.CartRepository;
import com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories.CartStatusRepository;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartQueryServiceImpl")
class CartQueryServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartStatusRepository cartStatusRepository;
    @Mock private IamContextFacade iamContextFacade;

    @InjectMocks private CartQueryServiceImpl service;

    private CartStatus activeStatus;
    private Cart sampleCart;

    @BeforeEach
    void setUp() {
        activeStatus = new CartStatus(CartStatuses.ACTIVE, "Active");
        sampleCart = new Cart(1L, activeStatus);
    }

    @Nested
    @DisplayName("Get Cart By Id")
    class GetCartByIdTests {

        @Test
        @DisplayName("should return cart when found")
        void shouldReturnCart() {
            when(cartRepository.findById(1L)).thenReturn(Optional.of(sampleCart));

            var result = service.handle(new GetCartByIdQuery(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmpty() {
            when(cartRepository.findById(99L)).thenReturn(Optional.empty());

            var result = service.handle(new GetCartByIdQuery(99L));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Cart By User Id")
    class GetCartByUserIdTests {

        @Test
        @DisplayName("should return any cart for manager")
        void shouldReturnAnyCartForManager() {
            when(iamContextFacade.currentUserHasRole("ROLE_MANAGER")).thenReturn(true);
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(sampleCart));

            var result = service.handle(new GetCartByUserIdQuery(1L));

            assertThat(result).isPresent();
            verify(cartRepository).findByUserId(1L);
            verify(cartRepository, never()).findByUserIdAndStatus(anyLong(), any());
        }

        @Test
        @DisplayName("should return only active cart for non-manager")
        void shouldReturnActiveCartForNonManager() {
            when(iamContextFacade.currentUserHasRole("ROLE_MANAGER")).thenReturn(false);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.of(sampleCart));

            var result = service.handle(new GetCartByUserIdQuery(1L));

            assertThat(result).isPresent();
            verify(cartRepository).findByUserIdAndStatus(1L, activeStatus);
        }

        @Test
        @DisplayName("should throw when active status not found for non-manager")
        void shouldThrowWhenActiveStatusNotFound() {
            when(iamContextFacade.currentUserHasRole("ROLE_MANAGER")).thenReturn(false);
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new GetCartByUserIdQuery(1L)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Active cart status not found");
        }

        @Test
        @DisplayName("should validate user access")
        void shouldValidateUserAccess() {
            when(iamContextFacade.currentUserHasRole("ROLE_MANAGER")).thenReturn(true);
            when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());

            service.handle(new GetCartByUserIdQuery(1L));

            verify(iamContextFacade).validateUserCanAccessResource(1L);
        }
    }


    @Nested
    @DisplayName("Get Current User Cart")
    class GetCurrentUserCartTests {

        @Test
        @DisplayName("should return active cart for current user")
        void shouldReturnActiveCart() {
            when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.of(sampleCart));

            var result = service.handle(new GetCurrentUserCartQuery());

            assertThat(result).isPresent();
            assertThat(result.get().getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should return empty when no active cart")
        void shouldReturnEmptyWhenNoCart() {
            when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.of(activeStatus));
            when(cartRepository.findByUserIdAndStatus(1L, activeStatus)).thenReturn(Optional.empty());

            var result = service.handle(new GetCurrentUserCartQuery());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should throw when not authenticated")
        void shouldThrowWhenNotAuthenticated() {
            when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new GetCurrentUserCartQuery()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not authenticated");
        }

        @Test
        @DisplayName("should throw when active status not found")
        void shouldThrowWhenActiveStatusNotFound() {
            when(iamContextFacade.getCurrentUserId()).thenReturn(Optional.of(1L));
            when(cartStatusRepository.findByName(CartStatuses.ACTIVE.name())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.handle(new GetCurrentUserCartQuery()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Active cart status not found");
        }
    }
}

