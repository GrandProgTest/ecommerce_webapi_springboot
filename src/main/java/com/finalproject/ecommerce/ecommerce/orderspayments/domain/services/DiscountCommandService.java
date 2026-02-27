package com.finalproject.ecommerce.ecommerce.orderspayments.domain.services;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CreateDiscountCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.ToggleDiscountStatusCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.Discount;

public interface DiscountCommandService {
    Discount handle(CreateDiscountCommand command);

    boolean handle(ToggleDiscountStatusCommand command);
}

