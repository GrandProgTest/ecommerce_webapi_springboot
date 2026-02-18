package com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "authorization.jwt")
public class JwtProperties {

    @NotBlank(message = "JWT secret is required")
    private String secret;

    @NotNull(message = "JWT expiration minutes is required")
    @Min(value = 1, message = "JWT expiration minutes must be at least 1")
    private Integer expirationMinutes;

    private AccessToken accessToken = new AccessToken();
    private RefreshToken refreshToken = new RefreshToken();

    @Data
    public static class AccessToken {
        @NotNull(message = "Access token expiration minutes is required")
        @Min(value = 1, message = "Access token expiration minutes must be at least 1")
        private Integer expirationMinutes;
    }

    @Data
    public static class RefreshToken {
        @NotNull(message = "Refresh token expiration days is required")
        @Min(value = 1, message = "Refresh token expiration days must be at least 1")
        private Integer expirationDays;
    }
}
