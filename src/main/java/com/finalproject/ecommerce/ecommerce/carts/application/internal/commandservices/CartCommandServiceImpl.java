package com.finalproject.ecommerce.ecommerce.carts.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.carts.domain.exceptions.CartNotFoundException;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.entities.CartStatus;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.valueobjects.CartStatuses;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartCommandService;
import com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories.CartRepository;
import com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories.CartStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class CartCommandServiceImpl implements CartCommandService {

    private final CartRepository cartRepository;
    private final CartStatusRepository cartStatusRepository;

    @Override
    @Transactional
    public Cart handle(AddProductToCartCommand command) {
        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), CartStatuses.ACTIVE)
            .orElseGet(() -> {
                Cart newCart = new Cart(command.userId());
                return cartRepository.save(newCart);
            });

        cart.addProduct(command.productId(), command.quantity());

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart handle(UpdateCartItemQuantityCommand command) {
        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), CartStatuses.ACTIVE)
            .orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + command.userId()));

        cart.updateProductQuantity(command.productId(), command.quantity());

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart handle(RemoveProductFromCartCommand command) {
        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), CartStatuses.ACTIVE)
            .orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + command.userId()));

        cart.removeProduct(command.productId());

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart handle(ClearCartCommand command) {
        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), CartStatuses.ACTIVE)
            .orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + command.userId()));

        cart.clear();

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart handle(CheckoutCartCommand command) {
        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), CartStatuses.ACTIVE)
            .orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + command.userId()));

        cart.checkout();

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void handle(SeedCartStatusCommand command) {
        Arrays.stream(CartStatuses.values()).forEach(status -> {
            if (!cartStatusRepository.existsByName(status.name())) {
                String description = switch (status) {
                    case ACTIVE -> "Cart is active and can be modified";
                    case CHECKED_OUT -> "Cart has been checked out and converted to an order";
                    case ABANDONED -> "Cart has been abandoned by the user";
                };
                cartStatusRepository.save(new CartStatus(status.name(), description));
            }
        });
    }
}
