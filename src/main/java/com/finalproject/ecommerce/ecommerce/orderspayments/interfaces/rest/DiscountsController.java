package com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.CreateDiscountCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands.ToggleDiscountStatusCommand;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.entities.Discount;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetAllDiscountsQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetDiscountByCodeQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.queries.GetDiscountByIdQuery;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.DiscountCommandService;
import com.finalproject.ecommerce.ecommerce.orderspayments.domain.services.DiscountQueryService;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.mapper.DiscountRestMapper;
import com.finalproject.ecommerce.ecommerce.orderspayments.interfaces.rest.mapper.DiscountRestMapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/discounts", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Discounts", description = "Discount Management Endpoints")
public class DiscountsController {

    private final DiscountCommandService discountCommandService;
    private final DiscountQueryService discountQueryService;

    public DiscountsController(DiscountCommandService discountCommandService, DiscountQueryService discountQueryService) {
        this.discountCommandService = discountCommandService;
        this.discountQueryService = discountQueryService;
    }

    @PostMapping
    @PreAuthorize("denyAll()")
    @Operation(summary = "[DISABLED] Create discount", description = "This REST endpoint is disabled. Please use GraphQL API instead")
    public ResponseEntity<DiscountResource> createDiscount(@RequestBody CreateDiscountResource resource) {
        CreateDiscountCommand command = DiscountRestMapper.toCreateCommand(resource);
        Discount discount = discountCommandService.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(DiscountRestMapper.toResource(discount));
    }

    @GetMapping
    @PreAuthorize("denyAll()")
    @Operation(summary = "[DISABLED] Get all discounts", description = "This REST endpoint is disabled. Please use GraphQL API instead")
    public ResponseEntity<List<DiscountResource>> getAllDiscounts() {
        List<Discount> discounts = discountQueryService.handle(new GetAllDiscountsQuery());
        return ResponseEntity.ok(DiscountRestMapper.toResourceList(discounts));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("denyAll()")
    @Operation(summary = "[DISABLED] Get discount by code", description = "This REST endpoint is disabled. Please use GraphQL API instead")
    public ResponseEntity<DiscountResource> getDiscountByCode(@PathVariable String code) {
        return discountQueryService.handle(new GetDiscountByCodeQuery(code))
                .map(discount -> ResponseEntity.ok(DiscountRestMapper.toResource(discount)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{discountId}/toggle-activate")
    @PreAuthorize("denyAll()")
    @Operation(summary = "[DISABLED] Toggle discount status (activate/deactivate)", description = "This REST endpoint is disabled. Please use GraphQL API instead")
    public ResponseEntity<DiscountRestMapper.ToggleDiscountStatusResponse> toggleDiscountStatus(@PathVariable Long discountId) {
        var command = DiscountRestMapper.toToggleCommand(discountId);
        boolean isActive = discountCommandService.handle(command);
        var discount = discountQueryService.handle(new GetDiscountByIdQuery(discountId))
                .orElseThrow(() -> new IllegalArgumentException("Discount not found with ID: " + discountId));

        String message = isActive
                ? "Discount activated successfully"
                : "Discount deactivated successfully";

        return ResponseEntity.ok(new DiscountRestMapper.ToggleDiscountStatusResponse(
                discount.getId(),
                discount.getCode(),
                discount.getIsActive(),
                message
        ));
    }
}

