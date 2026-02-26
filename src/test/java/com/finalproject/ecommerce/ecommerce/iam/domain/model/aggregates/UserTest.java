package com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.valueobjects.Roles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Aggregate")
class UserTest {

    private Role clientRole;
    private Role managerRole;

    @BeforeEach
    void setUp() {
        clientRole = new Role(Roles.ROLE_CLIENT);
        managerRole = new Role(Roles.ROLE_MANAGER);
    }

    @Nested
    @DisplayName("Creation")
    class CreationTests {

        @Test
        @DisplayName("should create user with all fields and inactive by default")
        void shouldCreateUserInactiveByDefault() {
            String username = "testuser";
            String email = "test@example.com";
            String password = "encodedPass123";

            User user = new User(username, email, password, clientRole);

            assertThat(user.getUsername()).isEqualTo(username);
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getPassword()).isEqualTo(password);
            assertThat(user.getRole()).isEqualTo(clientRole);
            assertThat(user.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("should create user with default constructor as inactive")
        void shouldCreateDefaultUserInactive() {
            User user = new User();

            assertThat(user.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("should create user with manager role")
        void shouldCreateWithManagerRole() {
            User user = new User("admin", "admin@ravn.co", "pass12345", managerRole);

            assertThat(user.getRole().getName()).isEqualTo(Roles.ROLE_MANAGER);
        }
    }

    @Nested
    @DisplayName("Activation")
    class ActivationTests {

        @Test
        @DisplayName("should activate user account")
        void shouldActivateUser() {
            User user = new User("testuser", "test@example.com", "pass12345", clientRole);

            user.activate();

            assertThat(user.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("should deactivate user account")
        void shouldDeactivateUser() {
            User user = new User("testuser", "test@example.com", "pass12345", clientRole);
            user.activate();

            user.deactivate();

            assertThat(user.getIsActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Collections")
    class CollectionTests {

        @Test
        @DisplayName("should initialize empty lists on creation")
        void shouldInitializeEmptyLists() {
            User user = new User("testuser", "test@example.com", "pass12345", clientRole);

            assertThat(user.getAddresses()).isNotNull().isEmpty();
            assertThat(user.getRefreshTokens()).isNotNull().isEmpty();
            assertThat(user.getUserTokens()).isNotNull().isEmpty();
        }
    }
}

