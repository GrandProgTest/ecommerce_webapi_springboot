package com.finalproject.ecommerce.ecommerce.shared.domain.model.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPriceValidator.class)
@Documented
public @interface ValidPrice {

    String message() default "Price must be at least 1.00 and have at most 2 decimal places";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    double minValue() default 1.0;

    int maxDecimalPlaces() default 2;
}
