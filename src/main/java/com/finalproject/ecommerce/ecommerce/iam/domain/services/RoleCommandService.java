package com.finalproject.ecommerce.ecommerce.iam.domain.services;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
    public void handle(SeedRolesCommand command);
}
