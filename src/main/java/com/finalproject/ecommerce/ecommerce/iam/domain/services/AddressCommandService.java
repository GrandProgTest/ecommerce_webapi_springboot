package com.finalproject.ecommerce.ecommerce.iam.domain.services;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.CreateAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.DeleteAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SetDefaultAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.UpdateAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Address;

import java.util.Optional;

public interface AddressCommandService {
    Address handle(CreateAddressCommand command, Long userId);

    Optional<Address> handle(UpdateAddressCommand command, Long userId);

    void handle(DeleteAddressCommand command);

    Optional<Address> handle(SetDefaultAddressCommand command);
}
