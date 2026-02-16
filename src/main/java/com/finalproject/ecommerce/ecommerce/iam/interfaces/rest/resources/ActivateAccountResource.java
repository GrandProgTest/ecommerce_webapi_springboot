package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record ActivateAccountResource(String activationToken) {
}

