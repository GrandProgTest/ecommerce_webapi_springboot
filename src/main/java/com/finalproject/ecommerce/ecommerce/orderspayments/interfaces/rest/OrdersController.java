package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.UpdateOrderStatusCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CancelOrderCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetAllOrdersQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetOrdersByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderCommandService;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderQueryService;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources.CreateOrderResource;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources.OrderResource;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources.UpdateOrderStatusResource;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.transform.CreateOrderCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.transform.OrderResourceFromEntityAssembler;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.transform.UpdateOrderStatusCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.shared.interfaces.rest.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/orders", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Orders", description = "Order Management Endpoints")
public class OrdersController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    public OrdersController(OrderCommandService orderCommandService, OrderQueryService orderQueryService) {
        this.orderCommandService = orderCommandService;
        this.orderQueryService = orderQueryService;
    }

    @PostMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create order (checkout)", description = "Creates an order from the user's active cart. This completes the checkout process.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully", content = @Content(schema = @Schema(implementation = OrderResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or cart is empty", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})

    public ResponseEntity<OrderResource> createOrder(@PathVariable Long userId, @RequestBody CreateOrderResource resource) {
        var command = CreateOrderCommandFromResourceAssembler.toCommandFromResource(userId, resource);
        var order = orderCommandService.handle(command);
        var orderResource = OrderResourceFromEntityAssembler.toResourceFromEntity(order);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResource);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "List all orders", description = "Returns all orders in the system. Only accessible by managers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully", content = @Content(schema = @Schema(implementation = OrderResource.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - manager role required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})

    public ResponseEntity<List<OrderResource>> getAllOrders() {
        var query = new GetAllOrdersQuery();
        var orders = orderQueryService.handle(query);
        var orderResources = orders.stream()
                .map(OrderResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(orderResources);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user's orders", description = "Returns all orders for the specified user. Managers can view any user's orders, clients can only view their own.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully", content = @Content(schema = @Schema(implementation = OrderResource.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - user can only access their own orders", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})

    public ResponseEntity<List<OrderResource>> getOrdersByUserId(@PathVariable Long userId) {
        var query = new GetOrdersByUserIdQuery(userId);
        var orders = orderQueryService.handle(query);
        var orderResources = orders.stream()
                .map(OrderResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(orderResources);
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Update order delivery status",
               description = "Update order status to SHIPPED or DELIVERED. Only accessible by managers. Order must be PAID before status can be updated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status updated successfully", content = @Content(schema = @Schema(implementation = OrderResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status or order is not paid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - manager role required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<OrderResource> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusResource resource) {

        var command = UpdateOrderStatusCommandFromResourceAssembler.toCommandFromResource(orderId, resource);
        var updatedOrder = orderCommandService.handle(command);
        var orderResource = OrderResourceFromEntityAssembler.toResourceFromEntity(updatedOrder);

        return ResponseEntity.ok(orderResource);
    }

    @DeleteMapping("/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel order",
               description = "Cancel an order. Only PENDING orders can be cancelled. Users can only cancel their own orders.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled successfully", content = @Content(schema = @Schema(implementation = OrderResource.class))),
            @ApiResponse(responseCode = "400", description = "Order cannot be cancelled (already paid/shipped/delivered)", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - can only cancel own orders", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<OrderResource> cancelOrder(@PathVariable Long orderId) {

        var command = new CancelOrderCommand(orderId);
        var cancelledOrder = orderCommandService.handle(command);
        var orderResource = OrderResourceFromEntityAssembler.toResourceFromEntity(cancelledOrder);

        return ResponseEntity.ok(orderResource);
    }
}
