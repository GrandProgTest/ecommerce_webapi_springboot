package com.finalproject.ecommerce.ecommerce.notifications.domain.model.valueobjects;

public record EmailAddress(String address) {
    public EmailAddress {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be null or empty");
        }
        if (!isValidEmail(address)) {
            throw new IllegalArgumentException("Invalid email address format: " + address);
        }
    }

    private static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    @Override
    public String toString() {
        return address;
    }
}

