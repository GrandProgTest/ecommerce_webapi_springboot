package com.finalproject.ecommerce.ecommerce.orderspayments.domain.services;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.Discount;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetAllDiscountsQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetDiscountByCodeQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetDiscountByIdQuery;

import java.util.List;
import java.util.Optional;

public interface DiscountQueryService {
    List<Discount> handle(GetAllDiscountsQuery query);
    Optional<Discount> handle(GetDiscountByCodeQuery query);
    Optional<Discount> handle(GetDiscountByIdQuery query);
}

