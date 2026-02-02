package com.finalproject.ecommerce.ecommerce.iam.application.acl.services;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetUserByIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetUserByUsernameQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserQueryService;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import org.apache.logging.log4j.util.Strings;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IamContextFacadeImpl implements IamContextFacade {
    private final UserQueryService userQueryService;

    public IamContextFacadeImpl(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
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
}
