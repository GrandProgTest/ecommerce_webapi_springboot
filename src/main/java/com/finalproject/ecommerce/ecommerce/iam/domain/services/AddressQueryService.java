package com.finalproject.ecommerce.ecommerce.iam.domain.services;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Address;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetAddressByIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetAddressesByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetDefaultAddressByUserIdQuery;

import java.util.List;
import java.util.Optional;

public interface AddressQueryService {
    Optional<Address> handle(GetAddressByIdQuery query);

    List<Address> handle(GetAddressesByUserIdQuery query);

    Optional<Address> handle(GetDefaultAddressByUserIdQuery query);
}
