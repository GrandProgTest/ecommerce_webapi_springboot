package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.ToggleProductLikeCommand;

public class ToggleProductLikeCommandFromResourceAssembler {

    public static ToggleProductLikeCommand toCommandFromResource(Long userId, Long productId) {
        return new ToggleProductLikeCommand(userId, productId);
    }
}

