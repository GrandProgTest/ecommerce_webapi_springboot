package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Address;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.AddressResource;

public class AddressResourceFromEntityAssembler {
    public static AddressResource toResourceFromEntity(Address address) {
        return new AddressResource(
                address.getId(),
                address.getUserId(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getPostalCode(),
                address.getIsDefault()
        );
    }
}
