package com.finalproject.ecommerce.ecommerce.carts.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.carts.domain.model.queries.GetCartByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartCommandService;
import com.finalproject.ecommerce.ecommerce.carts.domain.services.CartQueryService;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.mapper.CartRestMapper;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.mapper.CartRestMapper.AddItemToCartResource;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.mapper.CartRestMapper.CartResource;
import com.finalproject.ecommerce.ecommerce.carts.interfaces.rest.mapper.CartRestMapper.UpdateCartItemQuantityResource;
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
    @PreAuthorize("denyAll()")
    @Operation(summary = "[DISABLED] Get user's cart", description = "This REST endpoint is disabled. Please use GraphQL API instead")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "This REST endpoint is disabled - use GraphQL API"),
            @ApiResponse(responseCode = "200", description = "Cart found or empty cart returned"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")})
    public ResponseEntity<CartResource> getCartByUserId(@PathVariable Long userId) {
        var query = new GetCartByUserIdQuery(userId);
        var cart = cartQueryService.handle(query);

        return cart.map(c -> ResponseEntity.ok(CartRestMapper.toResource(c)))
                .orElseGet(() -> ResponseEntity.ok(null));
    }

    @PostMapping(value = "/{userId}/items", consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("denyAll()")
    @Operation(summary = "[DISABLED] Add item to cart", description = "This REST endpoint is disabled. Please use GraphQL API instead")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "This REST endpoint is disabled - use GraphQL API"),
            @ApiResponse(responseCode = "200", description = "Item added to cart successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product ID or quantity, or insufficient stock"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Product not found")})
    public ResponseEntity<CartResource> addItemToCart(
            @PathVariable Long userId,
            @RequestBody AddItemToCartResource resource) {

        var command = CartRestMapper.toAddItemCommand(userId, resource);
        var cart = cartCommandService.handle(command);
        return ResponseEntity.ok(CartRestMapper.toResource(cart));
    }

    @PutMapping("/{userId}/items/{cartItemId}")
    @PreAuthorize("denyAll()")
    @Operation(summary = "[DISABLED] Update cart item quantity", description = "This REST endpoint is disabled. Please use GraphQL API instead")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "This REST endpoint is disabled - use GraphQL API"),
            @ApiResponse(responseCode = "200", description = "Cart item quantity updated"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity or insufficient stock"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")})
    public ResponseEntity<CartResource> updateCartItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemQuantityResource resource) {

        var command = CartRestMapper.toUpdateQuantityCommand(userId, cartItemId, resource);
        var cart = cartCommandService.handle(command);
        return ResponseEntity.ok(CartRestMapper.toResource(cart));
    }

    @DeleteMapping("/{userId}/items/{cartItemId}")
    @PreAuthorize("denyAll()")
    @Operation(summary = "[DISABLED] Remove item from cart", description = "This REST endpoint is disabled. Please use GraphQL API instead")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "This REST endpoint is disabled - use GraphQL API"),
            @ApiResponse(responseCode = "200", description = "Cart item removed"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Cart item not found")})
    public ResponseEntity<CartResource> removeCartItem(
            @PathVariable Long userId,
            @PathVariable Long cartItemId) {

        var command = CartRestMapper.toRemoveItemCommand(userId, cartItemId);
        var cart = cartCommandService.handle(command);
        return ResponseEntity.ok(CartRestMapper.toResource(cart));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("denyAll()")
    @Operation(summary = "[DISABLED] Clear cart", description = "This REST endpoint is disabled. Please use GraphQL API instead")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "This REST endpoint is disabled - use GraphQL API"),
            @ApiResponse(responseCode = "200", description = "Cart cleared successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Cart not found")})
    public ResponseEntity<CartResource> clearCart(@PathVariable Long userId) {
        var command = CartRestMapper.toClearCartCommand(userId);
        var cart = cartCommandService.handle(command);
        return ResponseEntity.ok(CartRestMapper.toResource(cart));
    }
}
