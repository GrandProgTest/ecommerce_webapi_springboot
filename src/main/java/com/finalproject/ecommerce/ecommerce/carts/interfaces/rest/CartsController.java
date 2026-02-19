package com.finalproject.ecommerce.ecommerce.carts.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartCommandService;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartQueryService;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.resources.AddItemToCartResource;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.resources.CartResource;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.resources.UpdateCartItemQuantityResource;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.transform.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/cart", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Cart", description = "Cart Management Endpoints")
public class CartsController {

    private final CartCommandService cartCommandService;
    private final CartQueryService cartQueryService;

    public CartsController(CartCommandService cartCommandService, CartQueryService cartQueryService) {
        this.cartCommandService = cartCommandService;
        this.cartQueryService = cartQueryService;
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user's cart", description = "Returns the active cart for the specified user. Managers can view any cart, clients can only view their own cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart found or empty cart returned"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - user can only access their own cart")})
    public ResponseEntity<CartResource> getCartByUserId(@PathVariable Long userId) {
        var query = new GetCartByUserIdQuery(userId);
        var cart = cartQueryService.handle(query);

        return cart.map(c -> ResponseEntity.ok(CartResourceFromEntityAssembler.toResourceFromEntity(c)))
                .orElseGet(() -> ResponseEntity.ok(null));
    }

    @PostMapping(value = "/{userId}/items", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add item to cart", description = "Adds a product to the user's cart with the specified quantity. Managers can add to any cart, clients can only add to their own cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item added to cart successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product ID or quantity, or insufficient stock"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - user can only modify their own cart"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<CartResource> addItemToCart(
            @PathVariable Long userId,
            @RequestBody AddItemToCartResource resource) {

        var command = AddItemToCartCommandFromResourceAssembler.toCommandFromResource(userId, resource);
        var cart = cartCommandService.handle(command);
        var cartResource = CartResourceFromEntityAssembler.toResourceFromEntity(cart);

        return ResponseEntity.ok(cartResource);
    }

    @PutMapping("/{userId}/items/{cartItemId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update cart item quantity", description = "Updates the quantity of a specific cart item. Managers can update any cart, clients can only update their own cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart item quantity updated"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity or insufficient stock"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - user can only modify their own cart"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")})
    public ResponseEntity<CartResource> updateCartItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemQuantityResource resource) {

        var command = UpdateCartItemQuantityByCartItemIdCommandFromResourceAssembler.toCommandFromResource(userId, cartItemId, resource);
        var cart = cartCommandService.handle(command);
        var cartResource = CartResourceFromEntityAssembler.toResourceFromEntity(cart);

        return ResponseEntity.ok(cartResource);
    }

    @DeleteMapping("/{userId}/items/{cartItemId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove item from cart", description = "Removes a specific cart item from the cart. Managers can remove from any cart, clients can only remove from their own cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart item removed"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - user can only modify their own cart"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")})
    public ResponseEntity<CartResource> removeCartItem(
            @PathVariable Long userId,
            @PathVariable Long cartItemId) {

        var command = RemoveCartItemCommandFromResourceAssembler.toCommandFromResource(userId, cartItemId);
        var cart = cartCommandService.handle(command);
        var cartResource = CartResourceFromEntityAssembler.toResourceFromEntity(cart);

        return ResponseEntity.ok(cartResource);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Clear cart", description = "Removes all items from the user's cart. Managers can clear any cart, clients can only clear their own cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart cleared successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - user can only modify their own cart"),
            @ApiResponse(responseCode = "404", description = "Cart not found")})
    public ResponseEntity<CartResource> clearCart(@PathVariable Long userId) {
        var command = ClearCartCommandFromResourceAssembler.toCommandFromResource(userId);
        var cart = cartCommandService.handle(command);
        var cartResource = CartResourceFromEntityAssembler.toResourceFromEntity(cart);

        return ResponseEntity.ok(cartResource);
    }
}
