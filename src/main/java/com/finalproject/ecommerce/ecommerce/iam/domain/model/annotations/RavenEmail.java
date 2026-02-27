package com.finalproject.ecommerce.ecommerce.iam.domain.model.annotations;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.validators.RavenEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RavenEmailValidator.class)
@Documented
public @interface RavenEmail {
    String message() default "Email must be a valid format and organizational emails must end with @ravn.co";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    boolean requireRavenDomain() default false;
}

