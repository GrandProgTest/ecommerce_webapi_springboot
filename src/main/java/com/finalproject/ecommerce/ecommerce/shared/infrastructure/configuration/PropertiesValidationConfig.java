package com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration;

import com.finalproject.ecommerce.ecommerce.shared.infrastructure.configuration.properties.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    AppProperties.class,
    DatabaseProperties.class,
    JwtProperties.class,
    StripeProperties.class,
    CloudinaryProperties.class,
    EmailProperties.class
})
public class PropertiesValidationConfig {
}

