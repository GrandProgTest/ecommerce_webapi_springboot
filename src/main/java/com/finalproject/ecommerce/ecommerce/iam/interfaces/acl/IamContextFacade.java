package com.finalproject.ecommerce.ecommerce.iam.interfaces.acl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IamContextFacade {

    Long fetchUserIdByUsername(String username);

    String fetchUsernameByUserId(Long userId);

    boolean userExists(Long userId);

    Optional<Long> getCurrentUserId();

    boolean currentUserHasRole(String roleName);

    void validateUserCanAccessResource(Long resourceUserId);

    void validateAddressBelongsToUser(Long addressId, Long userId);

    String getUserEmail(Long userId);

    Map<Long, String> getUserEmails(List<Long> userIds);
}
