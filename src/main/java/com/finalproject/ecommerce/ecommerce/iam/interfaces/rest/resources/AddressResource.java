package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources;

public record AddressResource(Long id, Long userId, String street, String city, String state, String country,
                              String postalCode, Boolean isDefault) {
}
