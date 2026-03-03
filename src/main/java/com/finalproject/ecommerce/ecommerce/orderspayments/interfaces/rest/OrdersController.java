package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CancelOrderCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.*;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderCommandService;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.OrderQueryService;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.mapper.OrderRestMapper;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.mapper.OrderRestMapper.*;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.InvalidPageSizeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

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
    @PreAuthorize("denyAll()")
    @Operation(summary = "[DISABLED] Create order (checkout)", description = "This REST endpoint is disabled. Please use GraphQL API instead")
    public ResponseEntity<OrderResource> createOrder(@PathVariable Long userId, @RequestBody CreateOrderResource resource) {
        var command = OrderRestMapper.toCreateCommand(userId, resource);
        var order = orderCommandService.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderRestMapper.toResource(order));
    }

    @GetMapping
    @PreAuthorize("denyAll()")
    @Operation(summary = "[DISABLED] Get all orders with pagination and filtering (Manager)", description = "This REST endpoint is disabled. Please use GraphQL API instead")
    public ResponseEntity<PaginatedOrderResponse> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String deliveryStatus,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Instant dateFrom,
            @RequestParam(required = false) Instant dateTo) {

        if (size != 20 && size != 50 && size != 100) throw new InvalidPageSizeException(size);
        return ResponseEntity.ok(OrderRestMapper.toPaginatedResponse(
                orderQueryService.handle(new GetAllOrdersWithPaginationQuery(page, size, sortBy, sortDirection, status, deliveryStatus, userId, dateFrom, dateTo))));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("denyAll()")
    @Operation(summary = "[DISABLED] Get user orders with pagination and sorting", description = "This REST endpoint is disabled. Please use GraphQL API instead")
    public ResponseEntity<PaginatedOrderResponse> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        if (size != 20 && size != 50 && size != 100) throw new InvalidPageSizeException(size);
        return ResponseEntity.ok(OrderRestMapper.toPaginatedResponse(
                orderQueryService.handle(new GetUserOrdersWithPaginationQuery(userId, page, size, sortBy, sortDirection))));
    }

    @PatchMapping("/{orderId}/delivery-status")
    @PreAuthorize("denyAll()")
    @Operation(summary = "[DISABLED] Update order delivery status", description = "This REST endpoint is disabled. Please use GraphQL API instead")
    public ResponseEntity<OrderResource> updateOrderDeliveryStatus(@PathVariable Long orderId, @RequestBody UpdateOrderDeliveryStatusResource resource) {
        var command = OrderRestMapper.toUpdateDeliveryCommand(orderId, resource);
        return ResponseEntity.ok(OrderRestMapper.toResource(orderCommandService.handle(command)));
    }

    @DeleteMapping("/{orderId}/cancel")
    @PreAuthorize("denyAll()")
    @Operation(summary = "[DISABLED] Cancel order", description = "This REST endpoint is disabled. Please use GraphQL API instead")
    public ResponseEntity<OrderResource> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(OrderRestMapper.toResource(orderCommandService.handle(new CancelOrderCommand(orderId))));
    }

    @PostMapping("/{orderId}/confirm-payment")
    @PreAuthorize("denyAll()")
    @Operation(summary = "[DISABLED] Confirm payment for order (Backend only)", description = "This REST endpoint is disabled. Please use GraphQL API instead")
    public ResponseEntity<?> confirmPayment(@PathVariable Long orderId, @RequestBody(required = false) ConfirmPaymentResource resource) {
        var command = OrderRestMapper.toConfirmPaymentCommand(orderId, resource);
        var order = orderCommandService.handle(command);
        return ResponseEntity.ok(Map.of(
                "message", "Payment processed. Check order status.",
                "order", OrderRestMapper.toResource(order),
                "note", "If status is still PENDING, the payment may require additional authentication."
        ));
    }
}
