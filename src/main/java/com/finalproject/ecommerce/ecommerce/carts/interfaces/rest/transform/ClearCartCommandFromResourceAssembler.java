package com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.ClearCartCommand;

public class ClearCartCommandFromResourceAssembler {

    public static ClearCartCommand toCommandFromResource(Long userId) {
        return new ClearCartCommand(userId);
    }
}

