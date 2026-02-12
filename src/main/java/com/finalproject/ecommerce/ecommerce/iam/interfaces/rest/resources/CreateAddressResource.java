package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources;
public record CreateAddressResource(
        String street,
        String city,
        String state,
        String country,
        String postalCode,
        Boolean isDefault
) {}
