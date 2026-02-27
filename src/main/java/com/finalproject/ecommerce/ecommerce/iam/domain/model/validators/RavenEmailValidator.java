package com.finalproject.ecommerce.ecommerce.iam.domain.model.validators;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.annotations.RavenEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class RavenEmailValidator implements ConstraintValidator<RavenEmail, String> {
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final String RAVEN_DOMAIN = "@ravn.co";
    private boolean requireRavenDomain;

    @Override
    public void initialize(RavenEmail constraintAnnotation) {
        this.requireRavenDomain = constraintAnnotation.requireRavenDomain();
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) return false;
        if (!Pattern.matches(EMAIL_PATTERN, email)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Email must be a valid email address")
                    .addConstraintViolation();
            return false;
        }
        if (requireRavenDomain && !email.toLowerCase().endsWith(RAVEN_DOMAIN)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Email must be a Raven organization email (@ravn.co)")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }

    public static boolean isRavenEmail(String email) {
        return email != null && email.toLowerCase().endsWith(RAVEN_DOMAIN);
    }
}
