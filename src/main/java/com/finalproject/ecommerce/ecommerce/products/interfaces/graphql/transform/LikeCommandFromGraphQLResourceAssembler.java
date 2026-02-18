package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.ToggleProductLikeCommand;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.LikeProductGraphQLInput;

public class LikeCommandFromGraphQLResourceAssembler {

    public static ToggleProductLikeCommand toCommandFromResource(LikeProductGraphQLInput input) {
        return new ToggleProductLikeCommand(input.userId(), input.productId());
    }

    public static ToggleProductLikeCommand toCommandFromIds(Long userId, Long productId) {
        return new ToggleProductLikeCommand(userId, productId);
    }
}

