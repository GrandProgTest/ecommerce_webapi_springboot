package com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.ClearCartCommand;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.graphql.resources.ClearCartGraphQLInput;

public class ClearCartCommandFromResourceAssembler {

    public static ClearCartCommand toCommandFromResource(ClearCartGraphQLInput input) {
        return new ClearCartCommand(input.userId());
    }
}

