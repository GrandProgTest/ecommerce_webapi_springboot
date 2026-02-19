package com.finalproject.ecommerce.ecommerce.products.interfaces.rest.transform;

import com.finalproject.ecommerce.ecommerce.products.domain.model.commands.SetProductSalePriceCommand;
import com.finalproject.ecommerce.ecommerce.products.interfaces.rest.resources.SetProductSalePriceResource;

public class SetProductSalePriceCommandFromResourceAssembler {
    public static SetProductSalePriceCommand toCommandFromResource(Long productId, SetProductSalePriceResource resource) {
        return new SetProductSalePriceCommand(
                productId,
                resource.salePrice(),
                resource.salePriceExpireDate()
        );
    }
}
