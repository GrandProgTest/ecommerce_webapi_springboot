package com.finalproject.ecommerce.ecommerce.iam.domain.model.validators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RavenEmailValidator")
class RavenEmailValidatorTest {

    @Nested
    @DisplayName("isRavenEmail")
    class IsRavenEmailTests {

        @Test
        @DisplayName("should return true for @ravn.co email")
        void shouldReturnTrueForRavenEmail() {
            String email = "admin@ravn.co";

            boolean result = RavenEmailValidator.isRavenEmail(email);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return true for uppercase @RAVN.CO")
        void shouldReturnTrueForUpperCase() {
            String email = "ADMIN@RAVN.CO";

            boolean result = RavenEmailValidator.isRavenEmail(email);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return true for mixed case @Ravn.Co")
        void shouldReturnTrueForMixedCase() {
            String email = "user@Ravn.Co";

            boolean result = RavenEmailValidator.isRavenEmail(email);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false for non-raven email")
        void shouldReturnFalseForNonRaven() {
            String email = "user@gmail.com";

            boolean result = RavenEmailValidator.isRavenEmail(email);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false for null")
        void shouldReturnFalseForNull() {
            assertThat(RavenEmailValidator.isRavenEmail(null)).isFalse();
        }

        @Test
        @DisplayName("should return false for similar domain like ravn.com")
        void shouldReturnFalseForSimilarDomain() {
            assertThat(RavenEmailValidator.isRavenEmail("user@ravn.com")).isFalse();
        }

        @Test
        @DisplayName("should return false for empty string")
        void shouldReturnFalseForEmpty() {
            assertThat(RavenEmailValidator.isRavenEmail("")).isFalse();
        }
    }
}

