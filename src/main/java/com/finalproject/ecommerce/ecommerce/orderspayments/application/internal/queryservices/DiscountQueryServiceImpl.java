package com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.queryservices;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.Discount;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetAllDiscountsQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetDiscountByCodeQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetDiscountByIdQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.DiscountQueryService;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories.DiscountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DiscountQueryServiceImpl implements DiscountQueryService {

    private final DiscountRepository discountRepository;

    public DiscountQueryServiceImpl(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Override
    public List<Discount> handle(GetAllDiscountsQuery query) {
        return discountRepository.findAll();
    }

    @Override
    public Optional<Discount> handle(GetDiscountByCodeQuery query) {
        return discountRepository.findByCode(query.code());
    }

    @Override
    public Optional<Discount> handle(GetDiscountByIdQuery query) {
        return discountRepository.findById(query.discountId());
    }
}

