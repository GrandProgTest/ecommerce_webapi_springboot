package com.finalproject.ecommerce.ecommerce.carts.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartCommandService;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartQueryService;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.resources.AddProductToCartResource;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.resources.CartResource;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.resources.UpdateCartItemQuantityResource;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.transform.AddProductToCartCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.transform.CartResourceFromEntityAssembler;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.transform.UpdateCartItemQuantityCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Tag(name = "Carts", description = "Cart Management Endpoints")
public class CartsController {

    private final CartCommandService cartCommandService;
    private final CartQueryService cartQueryService;


    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user's active cart", description = "Returns the active cart for a specific user")
    public ResponseEntity<CartResource> getCartByUserId(@PathVariable Long userId) {
        var query = new GetCartByUserIdQuery(userId);
        var cart = cartQueryService.handle(query);

        return cart.map(c -> ResponseEntity.ok(CartResourceFromEntityAssembler.toResourceFromEntity(c)))
                   .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/user/{userId}/items")
    @Operation(summary = "Add product to cart", description = "Adds a product to the user's cart or increases quantity if already exists")
    public ResponseEntity<CartResource> addProductToCart(
            @PathVariable Long userId,
            @RequestBody AddProductToCartResource resource) {

        var command = AddProductToCartCommandFromResourceAssembler.toCommandFromResource(userId, resource);
        var cart = cartCommandService.handle(command);
        var cartResource = CartResourceFromEntityAssembler.toResourceFromEntity(cart);

        return ResponseEntity.status(HttpStatus.CREATED).body(cartResource);
    }

    @PutMapping("/user/{userId}/items/{productId}")
    @Operation(summary = "Update cart item quantity", description = "Updates the quantity of a specific product in the cart")
    public ResponseEntity<CartResource> updateCartItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestBody UpdateCartItemQuantityResource resource) {

        var command = UpdateCartItemQuantityCommandFromResourceAssembler.toCommandFromResource(userId, productId, resource);
        var cart = cartCommandService.handle(command);
        var cartResource = CartResourceFromEntityAssembler.toResourceFromEntity(cart);

        return ResponseEntity.ok(cartResource);
    }


    @DeleteMapping("/user/{userId}/items/{productId}")
    @Operation(summary = "Remove product from cart", description = "Removes a specific product from the cart")
    public ResponseEntity<CartResource> removeProductFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {

        var command = new RemoveProductFromCartCommand(userId, productId);
        var cart = cartCommandService.handle(command);
        var cartResource = CartResourceFromEntityAssembler.toResourceFromEntity(cart);

        return ResponseEntity.ok(cartResource);
    }


    @DeleteMapping("/user/{userId}/items")
    @Operation(summary = "Clear cart", description = "Removes all items from the cart")
    public ResponseEntity<CartResource> clearCart(@PathVariable Long userId) {
        var command = new ClearCartCommand(userId);
        var cart = cartCommandService.handle(command);
        var cartResource = CartResourceFromEntityAssembler.toResourceFromEntity(cart);

        return ResponseEntity.ok(cartResource);
    }

    @PostMapping("/user/{userId}/checkout")
    @Operation(summary = "Checkout cart", description = "Marks the cart as checked out")
    public ResponseEntity<CartResource> checkoutCart(@PathVariable Long userId) {
        var command = new CheckoutCartCommand(userId);
        var cart = cartCommandService.handle(command);
        var cartResource = CartResourceFromEntityAssembler.toResourceFromEntity(cart);

        return ResponseEntity.ok(cartResource);
    }
}
