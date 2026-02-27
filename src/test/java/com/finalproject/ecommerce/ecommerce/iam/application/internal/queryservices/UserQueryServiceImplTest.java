package com.finalproject.ecommerce.ecommerce.iam.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetAllUsersQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetUserByIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetUserByUsernameQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.valueobjects.Roles;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserQueryServiceImpl")
class UserQueryServiceImplTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserQueryServiceImpl service;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("testuser", "test@example.com", "pass12345", new Role(Roles.ROLE_CLIENT));
    }

    @Nested
    @DisplayName("Get User By Id")
    class GetUserByIdTests {
        @Test
        @DisplayName("should return user when found")
        void shouldReturnUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

            var result = service.handle(new GetUserByIdQuery(1L));

            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmpty() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            var result = service.handle(new GetUserByIdQuery(99L));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get User By Username")
    class GetUserByUsernameTests {
        @Test
        @DisplayName("should return user when found")
        void shouldReturnUser() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

            var result = service.handle(new GetUserByUsernameQuery("testuser"));

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmpty() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

            var result = service.handle(new GetUserByUsernameQuery("unknown"));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get All Users")
    class GetAllUsersTests {
        @Test
        @DisplayName("should return all users")
        void shouldReturnAll() {
            var user2 = new User("user2", "u2@test.com", "pass12345", new Role(Roles.ROLE_MANAGER));
            when(userRepository.findAll()).thenReturn(List.of(sampleUser, user2));

            var result = service.handle(new GetAllUsersQuery());

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should return empty list when no users")
        void shouldReturnEmptyList() {
            when(userRepository.findAll()).thenReturn(List.of());

            var result = service.handle(new GetAllUsersQuery());

            assertThat(result).isEmpty();
        }
    }
}

