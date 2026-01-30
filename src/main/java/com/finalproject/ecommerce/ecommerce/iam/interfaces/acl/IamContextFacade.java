package com.finalproject.ecommerce.ecommerce.iam.interfaces.acl;

public interface IamContextFacade {

    Long fetchUserIdByUsername(String username);
    String fetchUsernameByUserId(Long userId);
    boolean userExists(Long userId);
}
