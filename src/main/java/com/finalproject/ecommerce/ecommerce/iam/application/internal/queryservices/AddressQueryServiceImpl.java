package com.finalproject.ecommerce.ecommerce.iam.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Address;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetAddressByIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetAddressesByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetDefaultAddressByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.AddressQueryService;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.PermissionValidationService;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.AddressRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressQueryServiceImpl implements AddressQueryService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final PermissionValidationService permissionValidationService;

    public AddressQueryServiceImpl(AddressRepository addressRepository, UserRepository userRepository, PermissionValidationService permissionValidationService) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.permissionValidationService = permissionValidationService;
    }

    @Override
    public Optional<Address> handle(GetAddressByIdQuery query) {
        Optional<Address> address = addressRepository.findById(query.addressId());
        address.ifPresent(a -> permissionValidationService.validateUserCanAccessResource(a.getUserId()));
        return address;
    }

    @Override
    public List<Address> handle(GetAddressesByUserIdQuery query) {
        permissionValidationService.validateUserCanAccessResource(query.userId());
        User user = userRepository.findById(query.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", query.userId()));
        return addressRepository.findByUser(user);
    }

    @Override
    public Optional<Address> handle(GetDefaultAddressByUserIdQuery query) {
        permissionValidationService.validateUserCanAccessResource(query.userId());
        User user = userRepository.findById(query.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", query.userId()));
        return addressRepository.findByUserAndIsDefaultTrue(user);
    }
}
