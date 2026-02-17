package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.SoftDeleteProductCommand;

public class SoftDeleteProductCommandFromResourceAssembler {

    public static SoftDeleteProductCommand toCommandFromResource(Long productId) {
        return new SoftDeleteProductCommand(productId);
    }
}

