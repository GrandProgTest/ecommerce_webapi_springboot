package com.finalproject.ecommerce.ecommerce.iam.domain.model.commands;

public record CreateAddressCommand(String street, String city, String state, String country, String postalCode,
                                   Boolean isDefault) {
}
