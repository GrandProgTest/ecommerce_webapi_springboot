package com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {

    private Api api = new Api();
    private Success success = new Success();
    private Cancel cancel = new Cancel();

    @NotBlank(message = "Stripe currency is required")
    private String currency = "usd";

    @Data
    public static class Api {
        @NotBlank(message = "Stripe API secret key is required")
        private String secretKey;

        @NotBlank(message = "Stripe webhook secret is required")
        private String webhookSecret;
    }

    @Data
    public static class Success {
        @NotBlank(message = "Stripe success URL is required")
        private String url;
    }

    @Data
    public static class Cancel {
        @NotBlank(message = "Stripe cancel URL is required")
        private String url;
    }
}

