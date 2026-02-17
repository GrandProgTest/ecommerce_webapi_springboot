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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
public class CartCommandServiceImpl implements CartCommandService {

    private final CartRepository cartRepository;
    private final CartStatusRepository cartStatusRepository;
    private final IamContextFacade iamContextFacade;
    private final ProductContextFacade productContextFacade;

    public CartCommandServiceImpl(CartRepository cartRepository, CartStatusRepository cartStatusRepository, IamContextFacade iamContextFacade, ProductContextFacade productContextFacade) {
        this.cartRepository = cartRepository;
        this.cartStatusRepository = cartStatusRepository;
        this.iamContextFacade = iamContextFacade;
        this.productContextFacade = productContextFacade;
    }

    @Override
    public Cart handle(AddProductToCartCommand command) {
        final Long userId;
        if (command.userId() == null) {
            userId = iamContextFacade.getCurrentUserId().orElseThrow(() -> new IllegalStateException("User not authenticated"));
        } else {
            iamContextFacade.validateUserCanAccessResource(command.userId());
            userId = command.userId();
        }

        if (!iamContextFacade.userExists(userId)) {
            throw new InvalidCartOperationException("User with ID " + userId + " does not exist");
        }

        Integer availableStock = productContextFacade.getProductStock(command.productId());
        if (availableStock == null || availableStock <= 0) {
            throw new InvalidCartOperationException("Product with ID " + command.productId() + " is not available or out of stock");
        }

        if (!productContextFacade.isProductActive(command.productId())) {
            throw new InvalidCartOperationException("Product with ID " + command.productId() + " is not available");
        }

        CartStatus activeStatus = cartStatusRepository.findByName(CartStatuses.ACTIVE.name()).orElseThrow(() -> new IllegalStateException("Active cart status not found"));

        Cart cart = cartRepository.findByUserIdAndStatus(userId, activeStatus).orElseGet(() -> {
            Cart newCart = new Cart(userId, activeStatus);
            return cartRepository.save(newCart);
        });

        int currentQuantityInCart = cart.getProductQuantity(command.productId());
        int totalRequestedQuantity = currentQuantityInCart + command.quantity();

        if (totalRequestedQuantity > availableStock) {
            if (currentQuantityInCart >= availableStock) {
                throw new InvalidCartOperationException(
                        "You already have the maximum available quantity (" + currentQuantityInCart +
                                ") of this product in your cart. Cannot add more items."
                );
            } else {
                int remainingSpace = availableStock - currentQuantityInCart;
                throw new InvalidCartOperationException(
                        "Cannot add " + command.quantity() + " items. You have " + currentQuantityInCart +
                                " in your cart and the product has only " + availableStock +
                                " in stock. You can add up to " + remainingSpace + " more item(s)."
                );
            }
        }

        cart.addProduct(command.productId(), command.quantity());

        return cartRepository.save(cart);
    }

    @Override
    public Cart handle(UpdateCartItemQuantityCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        CartStatus activeStatus = cartStatusRepository.findByName(CartStatuses.ACTIVE.name()).orElseThrow(() -> new IllegalStateException("Active cart status not found"));

        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), activeStatus).orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + command.userId()));

        Integer availableStock = productContextFacade.getProductStock(command.productId());
        if (availableStock == null || availableStock <= 0) {
            throw new InvalidCartOperationException("Product with ID " + command.productId() + " is out of stock");
        }

        if (command.quantity() > availableStock) {
            throw new InvalidCartOperationException(
                    "Cannot set quantity to " + command.quantity() + ". Product has only " +
                            availableStock + " in stock."
            );
        }

        cart.updateProductQuantity(command.productId(), command.quantity());

        return cartRepository.save(cart);
    }

    @Override
    public Cart handle(UpdateCartItemQuantityByCartItemIdCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        CartStatus activeStatus = cartStatusRepository.findByName(CartStatuses.ACTIVE.name()).orElseThrow(() -> new IllegalStateException("Active cart status not found"));

        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), activeStatus).orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + command.userId()));

        var cartItem = cart.getItems().stream().filter(item -> item.getId().equals(command.cartItemId())).findFirst().orElseThrow(() -> new InvalidCartOperationException("Cart item not found"));

        Integer availableStock = productContextFacade.getProductStock(cartItem.getProductId());
        if (availableStock == null || availableStock <= 0) {
            throw new InvalidCartOperationException("Product is out of stock");
        }

        if (command.quantity() > availableStock) {
            throw new InvalidCartOperationException(
                    "Cannot set quantity to " + command.quantity() + ". Product has only " +
                            availableStock + " in stock."
            );
        }

        cart.updateCartItemQuantity(command.cartItemId(), command.quantity());

        return cartRepository.save(cart);
    }

    @Override
    public Cart handle(RemoveProductFromCartCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        CartStatus activeStatus = cartStatusRepository.findByName(CartStatuses.ACTIVE.name()).orElseThrow(() -> new IllegalStateException("Active cart status not found"));

        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), activeStatus).orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + command.userId()));

        cart.removeProduct(command.productId());

        return cartRepository.save(cart);
    }

    @Override
    public Cart handle(RemoveCartItemCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        CartStatus activeStatus = cartStatusRepository.findByName(CartStatuses.ACTIVE.name()).orElseThrow(() -> new IllegalStateException("Active cart status not found"));

        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), activeStatus).orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + command.userId()));

        cart.removeCartItem(command.cartItemId());

        return cartRepository.save(cart);
    }

    @Override
    public Cart handle(ClearCartCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        CartStatus activeStatus = cartStatusRepository.findByName(CartStatuses.ACTIVE.name()).orElseThrow(() -> new IllegalStateException("Active cart status not found"));

        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), activeStatus).orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + command.userId()));

        cart.clear();

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart handle(CheckoutCartCommand command) {
        iamContextFacade.validateUserCanAccessResource(command.userId());

        CartStatus activeStatus = cartStatusRepository.findByName(CartStatuses.ACTIVE.name()).orElseThrow(() -> new IllegalStateException("Active cart status not found"));

        Cart cart = cartRepository.findByUserIdAndStatus(command.userId(), activeStatus).orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + command.userId()));

        CartStatus checkedOutStatus = cartStatusRepository.findByName(CartStatuses.CHECKED_OUT.name()).orElseThrow(() -> new IllegalStateException("Checked out cart status not found"));

        cart.checkout(checkedOutStatus);

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
                cartStatusRepository.save(new CartStatus(status, description));
            }
        });
    }
}
