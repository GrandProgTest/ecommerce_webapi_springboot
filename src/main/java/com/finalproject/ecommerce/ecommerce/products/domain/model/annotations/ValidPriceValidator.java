package com.finalproject.ecommerce.ecommerce.products.domain.model.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class ValidPriceValidator implements ConstraintValidator<ValidPrice, BigDecimal> {

    private double minValue;
    private int maxDecimalPlaces;

    @Override
    public void initialize(ValidPrice constraintAnnotation) {
        this.minValue = constraintAnnotation.minValue();
        this.maxDecimalPlaces = constraintAnnotation.maxDecimalPlaces();
    }

    @Override
    public boolean isValid(BigDecimal price, ConstraintValidatorContext context) {
        if (price == null) {
            return true;
        }

        if (price.compareTo(BigDecimal.valueOf(minValue)) < 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Price must be at least %.2f", minValue)
            ).addConstraintViolation();
            return false;
        }

        int decimalPlaces = price.scale();
        if (decimalPlaces > maxDecimalPlaces) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Price can have at most %d decimal places", maxDecimalPlaces)
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
