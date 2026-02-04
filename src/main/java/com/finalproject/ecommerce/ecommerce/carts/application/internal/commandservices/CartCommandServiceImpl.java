package com.finalproject.ecommerce.ecommerce.carts.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.carts.domain.exceptions.CartNotFoundException;
import com.finalproject.ecommerce.ecommerce.carts.domain.exceptions.InvalidCartOperationException;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.aggregates.Cart;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.entities.CartStatus;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.valueobjects.CartStatuses;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartCommandService;
import com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories.CartRepository;
import com.finalproject.ecommerce.ecommerce.carts.infrastructure.persistence.jpa.repositories.CartStatusRepository;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.products.interfaces.acl.ProductContextFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class CartCommandServiceImpl implements CartCommandService {

    private final CartRepository cartRepository;
    private final CartStatusRepository cartStatusRepository;
    private final IamContextFacade iamContextFacade;
    private final ProductContextFacade productContextFacade;

    @Override
    public Cart handle(AddProductToCartCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        if (!iamContextFacade.userExists(command.userId())) {
            throw new InvalidCartOperationException("User with ID " + command.userId() + " does not exist");
        }

        if (!productContextFacade.isProductAvailableForPurchase(command.productId(), command.quantity())) {
            throw new InvalidCartOperationException("Product with ID " + command.productId() + " is not available or does not have enough stock");
        }

        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), CartStatuses.ACTIVE)
            .orElseGet(() -> {
                Cart newCart = new Cart(command.userId());
                return cartRepository.save(newCart);
            });

        cart.addProduct(command.productId(), command.quantity());

        return cartRepository.save(cart);
    }

    @Override
    public Cart handle(UpdateCartItemQuantityCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), CartStatuses.ACTIVE)
            .orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + command.userId()));

        if (!productContextFacade.hasAvailableStock(command.productId(), command.quantity())) {
            throw new InvalidCartOperationException("Product with ID " + command.productId() + " does not have enough stock");
        }

        cart.updateProductQuantity(command.productId(), command.quantity());

        return cartRepository.save(cart);
    }

    @Override
    public Cart handle(UpdateCartItemQuantityByCartItemIdCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), CartStatuses.ACTIVE)
            .orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + command.userId()));

        var cartItem = cart.getItems().stream()
            .filter(item -> item.getId().equals(command.cartItemId()))
            .findFirst()
            .orElseThrow(() -> new InvalidCartOperationException("Cart item not found"));

        if (!productContextFacade.hasAvailableStock(cartItem.getProductId(), command.quantity())) {
            throw new InvalidCartOperationException("Product does not have enough stock");
        }

        cart.updateCartItemQuantity(command.cartItemId(), command.quantity());

        return cartRepository.save(cart);
    }

    @Override
    public Cart handle(RemoveProductFromCartCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), CartStatuses.ACTIVE)
            .orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + command.userId()));

        cart.removeProduct(command.productId());

        return cartRepository.save(cart);
    }

    @Override
    public Cart handle(RemoveCartItemCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), CartStatuses.ACTIVE)
            .orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + command.userId()));

        cart.removeCartItem(command.cartItemId());

        return cartRepository.save(cart);
    }

    @Override
    public Cart handle(ClearCartCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), CartStatuses.ACTIVE)
            .orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + command.userId()));

        cart.clear();

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart handle(CheckoutCartCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

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
