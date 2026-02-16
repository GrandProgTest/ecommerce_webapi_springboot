package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources;

public record ResetPasswordResource(String token, String password, String passwordConfirmation) {
}

