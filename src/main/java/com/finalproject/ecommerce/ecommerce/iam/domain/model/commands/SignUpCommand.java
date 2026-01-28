package com.finalproject.ecommerce.ecommerce.iam.domain.model.commands;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.valueobjects.Roles;

public record SignUpCommand(String username, String email, String password, Roles role) {
}
