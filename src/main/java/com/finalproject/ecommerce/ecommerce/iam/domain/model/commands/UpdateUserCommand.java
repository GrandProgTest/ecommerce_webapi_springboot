package com.finalproject.ecommerce.ecommerce.iam.domain.model.commands;

public record UpdateUserCommand(Long userId, String username, String password) {
}
