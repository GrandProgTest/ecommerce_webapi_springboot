package com.finalproject.ecommerce.ecommerce.iam.application.acl.services;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetUserByIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetUserByUsernameQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.PermissionValidationService;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserQueryService;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class IamContextFacadeImpl implements IamContextFacade {
    private final UserQueryService userQueryService;
    private final PermissionValidationService permissionValidationService;

    public IamContextFacadeImpl(UserQueryService userQueryService, PermissionValidationService permissionValidationService) {
        this.userQueryService = userQueryService;
        this.permissionValidationService = permissionValidationService;
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
        permissionValidationService.validateUserCanAccessResource(resourceUserId);
    }

    @Override
    public void validateAddressBelongsToUser(Long addressId, Long userId) {
        permissionValidationService.validateAddressBelongsToUser(addressId, userId);
    }

    @Override
    public String getUserEmail(Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var result = userQueryService.handle(getUserByIdQuery);
        return result.map(User::getEmail).orElse(Strings.EMPTY);
    }

    @Override
    public String getUsernameById(Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        return userQueryService.handle(getUserByIdQuery)
                .map(user -> user.getUsername())
                .orElse(null);
    }

    @Override
    public Map<Long, String> getUserEmails(List<Long> userIds) {
        Map<Long, String> emailMap = new HashMap<>();
        for (Long userId : userIds) {
            var getUserByIdQuery = new GetUserByIdQuery(userId);
            var result = userQueryService.handle(getUserByIdQuery);
            result.ifPresent(user -> emailMap.put(userId, user.getEmail()));
        }
        return emailMap;
    }
}
