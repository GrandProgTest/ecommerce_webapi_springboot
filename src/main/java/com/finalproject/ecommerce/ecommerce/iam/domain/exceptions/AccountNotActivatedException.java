package com.finalproject.ecommerce.ecommerce.iam.domain.exceptions;

public class AccountNotActivatedException extends RuntimeException {
    public AccountNotActivatedException(String username) {
        super("Account for user '" + username + "' is not activated. Please check your email to activate your account.");
    }
}

