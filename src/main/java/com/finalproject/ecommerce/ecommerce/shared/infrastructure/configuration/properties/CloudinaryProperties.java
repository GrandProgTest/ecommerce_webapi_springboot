package com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "cloudinary")
public class CloudinaryProperties {

    @NotBlank(message = "Cloudinary cloud name is required")
    private String cloudName;

    @NotBlank(message = "Cloudinary API key is required")
    private String apiKey;

    @NotBlank(message = "Cloudinary API secret is required")
    private String apiSecret;
}

