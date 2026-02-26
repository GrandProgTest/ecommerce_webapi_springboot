package com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "spring.datasource")
public class DatabaseProperties {

    @NotBlank(message = "Database URL is required")
    private String url;

    @NotBlank(message = "Database username is required")
    private String username;

    @NotBlank(message = "Database password is required")
    private String password;

    @NotBlank(message = "Database driver class name is required")
    private String driverClassName;
}

