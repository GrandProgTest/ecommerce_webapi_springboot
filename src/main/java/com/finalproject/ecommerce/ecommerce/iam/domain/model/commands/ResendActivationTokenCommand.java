package com.finalproject.ecommerce.ecommerce.iam.domain.model.commands;

public record ResendActivationTokenCommand(String email) {
    public ResendActivationTokenCommand {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
    }
}

