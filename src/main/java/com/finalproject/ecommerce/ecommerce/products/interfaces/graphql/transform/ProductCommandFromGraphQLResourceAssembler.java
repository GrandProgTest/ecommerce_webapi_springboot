package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeleteProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.ActivateProductCommand;
import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.DeactivateProductCommand;

public class ProductCommandFromGraphQLResourceAssembler {

    public static DeleteProductCommand toDeleteCommandFromId(Long id) {
        return new DeleteProductCommand(id);
    }

    public static ActivateProductCommand toActivateCommandFromId(Long id) {
        return new ActivateProductCommand(id);
    }

    public static DeactivateProductCommand toDeactivateCommandFromId(Long id) {
        return new DeactivateProductCommand(id);
    }
}

