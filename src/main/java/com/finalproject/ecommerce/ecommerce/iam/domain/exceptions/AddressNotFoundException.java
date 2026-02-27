package com.finalproject.ecommerce.ecommerce.iam.domain.exceptions;

import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.ResourceNotFoundException;

public class AddressNotFoundException extends ResourceNotFoundException {
    public AddressNotFoundException(Long addressId) {
        super("Address with id " + addressId + " not found");
    }
}
