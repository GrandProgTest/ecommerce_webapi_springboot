package com.finalproject.ecommerce.ecommerce.iam.interfaces.acl;

import java.util.Optional;

public interface IamContextFacade {

    Long fetchUserIdByUsername(String username);
    String fetchUsernameByUserId(Long userId);
    boolean userExists(Long userId);
    Optional<Long> getCurrentUserId();
}
