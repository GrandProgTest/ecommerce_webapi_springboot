package com.finalproject.ecommerce.ecommerce.iam.domain.model.commands;

public record UpdateAddressCommand(Long addressId, String street, String city, String state, String country,
                                   String postalCode) {
}
