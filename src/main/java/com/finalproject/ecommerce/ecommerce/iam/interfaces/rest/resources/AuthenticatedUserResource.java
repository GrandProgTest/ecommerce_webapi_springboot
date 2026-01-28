package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources;

public record AuthenticatedUserResource(Long id, String username, String email, String token) {
}
