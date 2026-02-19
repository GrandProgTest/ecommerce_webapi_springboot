package com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.SetProductSalePriceCommand;
import com.finalproject.ecommerce.ecommerce.products.interfaces.graphql.resources.SetProductSalePriceGraphQLInput;

import java.math.BigDecimal;

public class SetProductSalePriceCommandFromGraphQLInputAssembler {
    public static SetProductSalePriceCommand toCommandFromInput(Long productId, SetProductSalePriceGraphQLInput input) {
        return new SetProductSalePriceCommand(
                productId,
                BigDecimal.valueOf(input.salePrice()),
                input.salePriceExpireDate()
        );
    }
}
