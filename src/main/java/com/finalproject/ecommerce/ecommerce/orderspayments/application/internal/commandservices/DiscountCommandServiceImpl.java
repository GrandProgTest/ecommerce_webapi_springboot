package com.finalproject.ecommerce.ecommerce.orderspayments.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CreateDiscountCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.ToggleDiscountStatusCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.Discount;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.DiscountCommandService;
import com.finalproject.ecommerce.ecommerce.orderspayments.infrastructure.persistence.jpa.repositories.DiscountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiscountCommandServiceImpl implements DiscountCommandService {

    private final DiscountRepository discountRepository;

    public DiscountCommandServiceImpl(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Override
    @Transactional
    public Discount handle(CreateDiscountCommand command) {
        if (discountRepository.existsByCode(command.code())) {
            throw new IllegalArgumentException("Discount code already exists: " + command.code());
        }

        Discount discount = new Discount(command.code(), command.percentage(), command.startDate(), command.endDate());

        Discount savedDiscount = discountRepository.save(discount);
        return savedDiscount;
    }

    @Override
    @Transactional
    public boolean handle(ToggleDiscountStatusCommand command) {
        var discount = discountRepository.findById(command.discountId())
                .orElseThrow(() -> new IllegalArgumentException("Discount not found with ID: " + command.discountId()));

        try {
            boolean isActive = discount.getIsActive();

            if (isActive) {
                discount.deactivate();
            } else {
                discount.activate();
            }

            discountRepository.save(discount);
            return discount.getIsActive();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while toggling discount status: %s".formatted(e.getMessage()));
        }
    }
}

