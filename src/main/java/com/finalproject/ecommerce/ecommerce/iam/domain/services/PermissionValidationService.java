package com.finalproject.ecommerce.ecommerce.iam.domain.services;

public interface PermissionValidationService {

    void validateUserCanAccessResource(Long resourceUserId);

    void validateAddressBelongsToUser(Long addressId, Long userId);

    void validateManagerOrUserCanAccessResource(Long resourceUserId);
}
