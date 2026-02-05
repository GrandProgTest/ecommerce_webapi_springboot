package com.finalproject.ecommerce.ecommerce.orderspayments.rest;

import com.finalproject.ecommerce.ecommerce.carts.interfaces.acl.CartContextFacade;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.acl.IamContextFacade;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetAllOrdersQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetOrdersByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderCommandService;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderQueryService;
import com.finalproject.ecommerce.ecommerce.orderspayments.rest.resources.CreateOrderResource;
import com.finalproject.ecommerce.ecommerce.orderspayments.rest.resources.OrderResource;
import com.finalproject.ecommerce.ecommerce.orderspayments.rest.transform.CreateOrderCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.orderspayments.rest.transform.OrderResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/orders", produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order Management Endpoints")
public class OrdersController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;
    private final IamContextFacade iamContextFacade;
    private final CartContextFacade cartContextFacade;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create order (checkout)", description = "Creates an order from the user's active cart. This completes the checkout process.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or cart is empty"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<OrderResource> createOrder(@RequestBody CreateOrderResource resource) {
        var currentUserId = iamContextFacade.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        var cartDto = cartContextFacade.getActiveCartByUserId(currentUserId)
                .orElseThrow(() -> new IllegalStateException("No active cart found for user"));

        var command = CreateOrderCommandFromResourceAssembler.toCommandFromResource(
                currentUserId,
                cartDto.id(),
                resource
        );
        var order = orderCommandService.handle(command);
        var orderResource = OrderResourceFromEntityAssembler.toResourceFromEntity(order);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResource);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "List all orders", description = "Returns all orders in the system. Only accessible by managers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - manager role required")})
    public ResponseEntity<List<OrderResource>> getAllOrders() {
        var query = new GetAllOrdersQuery();
        var orders = orderQueryService.handle(query);
        var orderResources = orders.stream()
                .map(OrderResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(orderResources);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user's orders", description = "Returns all orders for the specified user. Managers can view any user's orders, clients can only view their own.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "403", description = "Access denied - user can only access their own orders")})
    public ResponseEntity<List<OrderResource>> getOrdersByUserId(@PathVariable Long userId) {
        var query = new GetOrdersByUserIdQuery(userId);
        var orders = orderQueryService.handle(query);
        var orderResources = orders.stream()
                .map(OrderResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(orderResources);
    }
}
