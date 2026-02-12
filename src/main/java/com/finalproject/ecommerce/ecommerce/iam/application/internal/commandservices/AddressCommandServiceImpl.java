package com.finalproject.ecommerce.ecommerce.iam.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.iam.domain.exceptions.AddressNotFoundException;
import com.finalproject.ecommerce.ecommerce.iam.domain.exceptions.UnauthorizedAddressAccessException;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Address;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.CreateAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.DeleteAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SetDefaultAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.UpdateAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.AddressCommandService;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.AddressRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AddressCommandServiceImpl implements AddressCommandService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final IamContextFacade iamContextFacade;

    public AddressCommandServiceImpl(AddressRepository addressRepository, UserRepository userRepository, IamContextFacade iamContextFacade) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    public Address handle(CreateAddressCommand command, Long userId) {
        iamContextFacade.validateUserCanAccessResource(userId);

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));

        if (command.isDefault() != null && command.isDefault()) {
            addressRepository.findByUser(user).forEach(address -> {
                address.unsetAsDefault();
                addressRepository.save(address);
            });
        }

        Address address = new Address(command, user);
        return addressRepository.save(address);
    }

    @Override
    public Optional<Address> handle(UpdateAddressCommand command, Long userId) {
        iamContextFacade.validateUserCanAccessResource(userId);

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));

        Optional<Address> addressOptional = addressRepository.findById(command.addressId());

        if (addressOptional.isEmpty()) {
            throw new AddressNotFoundException(command.addressId());
        }

        Address address = addressOptional.get();

        if (!address.belongsToUser(user)) {
            throw new UnauthorizedAddressAccessException();
        }

        address.updateAddress(command.street(), command.city(), command.state(), command.country(), command.postalCode());

        return Optional.of(addressRepository.save(address));
    }

    @Override
    public void handle(DeleteAddressCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        User user = userRepository.findById(command.userId()).orElseThrow(() -> new ResourceNotFoundException("User with id " + command.userId() + " not found"));

        Optional<Address> addressOptional = addressRepository.findById(command.addressId());

        if (addressOptional.isEmpty()) {
            throw new AddressNotFoundException(command.addressId());
        }

        Address address = addressOptional.get();

        if (!address.belongsToUser(user)) {
            throw new UnauthorizedAddressAccessException();
        }

        addressRepository.delete(address);
    }

    @Override
    public Optional<Address> handle(SetDefaultAddressCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        User user = userRepository.findById(command.userId()).orElseThrow(() -> new ResourceNotFoundException("User with id " + command.userId() + " not found"));

        Optional<Address> addressOptional = addressRepository.findById(command.addressId());

        if (addressOptional.isEmpty()) {
            throw new AddressNotFoundException(command.addressId());
        }

        Address address = addressOptional.get();

        if (!address.belongsToUser(user)) {
            throw new UnauthorizedAddressAccessException();
        }

        addressRepository.findByUser(user).forEach(addr -> {
            if (!addr.getId().equals(command.addressId())) {
                addr.unsetAsDefault();
                addressRepository.save(addr);
            }
        });

        address.setAsDefault();
        return Optional.of(addressRepository.save(address));
    }
}
