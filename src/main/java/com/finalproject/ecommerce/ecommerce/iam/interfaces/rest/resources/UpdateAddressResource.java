package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources;
public record UpdateAddressResource(
        String street,
        String city,
        String state,
        String country,
        String postalCode
) {}
