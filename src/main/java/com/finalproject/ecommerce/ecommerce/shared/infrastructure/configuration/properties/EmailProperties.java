package com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "spring.mail")
public class EmailProperties {

    @NotBlank(message = "Mail host is required")
    private String host;

    @NotNull(message = "Mail port is required")
    @Min(value = 1, message = "Mail port must be at least 1")
    @Max(value = 65535, message = "Mail port must be at most 65535")
    private Integer port;

    @NotBlank(message = "Mail username is required")
    private String username;

    @NotBlank(message = "Mail password is required")
    private String password;
}

