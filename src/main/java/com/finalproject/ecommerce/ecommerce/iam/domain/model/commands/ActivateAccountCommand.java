package com.finalproject.ecommerce.ecommerce.iam.domain.model.commands;

public record ActivateAccountCommand(String activationToken) {
    public ActivateAccountCommand {
        if (activationToken == null || activationToken.isBlank()) {
            throw new IllegalArgumentException("Activation token cannot be null or empty");
        }
    }
}

