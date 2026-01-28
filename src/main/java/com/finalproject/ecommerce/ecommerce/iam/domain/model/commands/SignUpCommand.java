package com.finalproject.ecommerce.ecommerce.iam.domain.model.commands;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;

public record SignUpCommand(String username, String email, String password, Role role) {
}
