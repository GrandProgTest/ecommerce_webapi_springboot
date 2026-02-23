package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CancelOrderCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.*;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderCommandService;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderQueryService;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.resources.*;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.transform.*;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.InvalidPageSizeException;
import com.finalproject.ecommerce.ecommerce.shared.interfaces.rest.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "Get all orders with pagination and filtering (Manager)",
            description = "Get paginated list of orders with sorting options and optional filters. Only accessible by managers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully with pagination metadata (may be empty list if no orders match filters)", content = @Content(schema = @Schema(implementation = PaginatedOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid page size - Allowed values are: 20, 50, 100", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - manager role required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<PaginatedOrderResponse> getAllOrders(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of orders per page (allowed: 20, 50, 100)", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field (createdAt, totalAmount, status)", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDirection,
            @Parameter(description = "Filter by order status (PENDING, PAID, CANCELLED) - optional", example = "PAID")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by delivery status (PACKED, SHIPPED, IN_TRANSIT, DELIVERED) - optional", example = "SHIPPED")
            @RequestParam(required = false) String deliveryStatus,
            @Parameter(description = "Filter by user ID - optional", example = "1")
            @RequestParam(required = false) Long userId) {

        if (size != 20 && size != 50 && size != 100) {
            throw new InvalidPageSizeException(size);
        }

        var response = OrderResourceFromEntityAssembler.toPaginatedResponse(
                orderQueryService.handle(new GetAllOrdersWithPaginationQuery(page, size, sortBy, sortDirection, status, deliveryStatus, userId))
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user orders with pagination and sorting",
               description = "Get paginated list of user's orders, sorted by most recent first. Includes order status and delivery tracking. Users can only view their own orders.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully with pagination metadata (may be empty list if user has no orders)", content = @Content(schema = @Schema(implementation = PaginatedOrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid page size - Allowed values are: 20, 50, 100", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - can only view own orders", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<PaginatedOrderResponse> getUserOrders(
            @PathVariable Long userId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of orders per page (allowed: 20, 50, 100)", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field (createdAt, totalAmount, status)", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDirection) {

        if (size != 20 && size != 50 && size != 100) {
            throw new InvalidPageSizeException(size);
        }

        var response = OrderResourceFromEntityAssembler.toPaginatedResponse(
                orderQueryService.handle(new GetUserOrdersWithPaginationQuery(userId, page, size, sortBy, sortDirection))
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/delivery-status")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @Operation(summary = "Update order delivery status",
            description = "Update order delivery status to PACKED, SHIPPED, IN_TRANSIT, or DELIVERED. Only accessible by managers. Order must be PAID before delivery status can be updated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order delivery status updated successfully", content = @Content(schema = @Schema(implementation = OrderResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status or order is not paid", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - manager role required", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<OrderResource> updateOrderDeliveryStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderDeliveryStatusResource resource) {

        var command = UpdateOrderDeliveryStatusCommandFromResourceAssembler.toCommandFromResource(orderId, resource);
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

    @PostMapping("/{orderId}/confirm-payment")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Confirm payment for order (Backend only)",
            description = "Confirms payment directly with Stripe using a payment method ID. Use 'pm_card_visa' for testing successful payments. This is for backend-only implementations without frontend.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment confirmed successfully", content = @Content(schema = @Schema(implementation = OrderResource.class))),
            @ApiResponse(responseCode = "400", description = "Order already paid or payment failed", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - can only pay for own orders", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<?> confirmPayment(
            @Parameter(description = "Order ID to confirm payment for", required = true, example = "1")
            @PathVariable Long orderId,
            @RequestBody(required = false) ConfirmPaymentResource resource) {

        var command = ConfirmPaymentCommandFromResourceAssembler.toCommandFromResource(orderId, resource);
        var order = orderCommandService.handle(command);
        var orderResource = OrderResourceFromEntityAssembler.toResourceFromEntity(order);

        return ResponseEntity.ok(java.util.Map.of(
                "message", "Payment processed. Check order status.",
                "order", orderResource,
                "note", "If status is still PENDING, the payment may require additional authentication. Check webhook logs or Stripe dashboard."
        ));
    }
}
