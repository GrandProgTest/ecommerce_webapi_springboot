package com.finalproject.ecommerce.ecommerce.iam.application.acl.services;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Address;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetAddressByIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetUserByIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetUserByUsernameQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.AddressQueryService;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserQueryService;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IamContextFacadeImpl implements IamContextFacade {
    private final UserQueryService userQueryService;
    private final AddressQueryService addressQueryService;

    public IamContextFacadeImpl(UserQueryService userQueryService, AddressQueryService addressQueryService) {
        this.userQueryService = userQueryService;
        this.addressQueryService = addressQueryService;
    }

    @Override
    public Long fetchUserIdByUsername(String username) {
        var getUserByUsernameQuery = new GetUserByUsernameQuery(username);
        var result = userQueryService.handle(getUserByUsernameQuery);
        if (result.isEmpty()) return 0L;
        return result.get().getId();
    }

    @Override
    public String fetchUsernameByUserId(Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var result = userQueryService.handle(getUserByIdQuery);
        if (result.isEmpty()) return Strings.EMPTY;
        return result.get().getUsername();
    }

    @Override
    public boolean userExists(Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var result = userQueryService.handle(getUserByIdQuery);
        return result.isPresent();
    }

    @Override
    public Optional<Long> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        String username = authentication.getName();
        var userId = fetchUserIdByUsername(username);

        if (userId == 0L) {
            return Optional.empty();
        }

        return Optional.of(userId);
    }

    @Override
    public boolean currentUserHasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority != null && authority.equals(roleName));
    }

    @Override
    public void validateUserCanAccessResource(Long resourceUserId) {
        var currentUserId = getCurrentUserId();

        if (currentUserId.isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException("User not authenticated");
        }

        if (currentUserHasRole("ROLE_MANAGER")) {
            return;
        }

        if (!currentUserId.get().equals(resourceUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("You don't have permission to access this resource");
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

        var addressQuery = new GetAddressByIdQuery(addressId);
        Optional<Address> addressOpt = addressQueryService.handle(addressQuery);

        if (addressOpt.isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException("Address with ID " + addressId + " not found");
        }

        Address address = addressOpt.get();

        if (!address.getUserId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Address with ID " + addressId + " does not belong to user with ID " + userId);
        }
    }
}
