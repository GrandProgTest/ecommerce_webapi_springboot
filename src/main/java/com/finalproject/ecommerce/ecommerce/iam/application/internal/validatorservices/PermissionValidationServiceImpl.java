package com.finalproject.ecommerce.ecommerce.iam.application.internal.validatorservices;

import com.finalproject.ecommerce.ecommerce.iam.domain.exceptions.AddressNotFoundException;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Address;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.PermissionValidationService;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.AddressRepository;
import com.finalproject.ecommerce.ecommerce.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class PermissionValidationServiceImpl implements PermissionValidationService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public PermissionValidationServiceImpl(UserRepository userRepository, AddressRepository addressRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    public void validateUserCanAccessResource(Long resourceUserId) {
        Optional<Long> currentUserId = getCurrentUserId();

        if (currentUserId.isEmpty()) {
            throw new AccessDeniedException("User not authenticated");
        }

        if (currentUserHasRole("ROLE_MANAGER")) {
            return;
        }

        if (!currentUserId.get().equals(resourceUserId)) {
            throw new AccessDeniedException("You don't have permission to access this resource");
        }
    }

    @Override
    public void validateAddressBelongsToUser(Long addressId, Long userId) {
        if (addressId == null) {
            throw new IllegalArgumentException("Address ID cannot be null");
        }

        if (addressId <= 0) {
            throw new IllegalArgumentException("Address ID must be a positive number");
        }

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        Optional<Address> addressOpt = addressRepository.findById(addressId);

        if (addressOpt.isEmpty()) {
            throw new AddressNotFoundException(addressId);
        }

        Address address = addressOpt.get();

        if (!address.getUserId().equals(userId)) {
            throw new AccessDeniedException("Address with ID " + addressId + " does not belong to user with ID " + userId);
        }
    }

    private Optional<Long> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        String username = authentication.getName();
        Optional<User> user = userRepository.findByUsername(username);

        return user.map(User::getId);
    }

    private boolean currentUserHasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority != null && authority.equals(roleName));
    }
}
