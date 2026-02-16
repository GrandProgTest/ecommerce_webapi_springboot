package com.finalproject.ecommerce.ecommerce.iam.domain.model.commands;

public record ResetPasswordCommand(String token, String password, String passwordConfirmation) {
}

