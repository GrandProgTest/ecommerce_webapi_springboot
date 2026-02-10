package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.DeleteAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SetDefaultAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetAddressByIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetAddressesByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetDefaultAddressByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.AddressCommandService;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.AddressQueryService;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.AddressResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.CreateAddressResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.UpdateAddressResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.AddressResourceFromEntityAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.CreateAddressCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.UpdateAddressCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/users/{userId}/addresses", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Addresses", description = "Address management endpoints")
public class AddressesController {

    private final AddressCommandService addressCommandService;
    private final AddressQueryService addressQueryService;

    public AddressesController(AddressCommandService addressCommandService,
                                AddressQueryService addressQueryService) {
        this.addressCommandService = addressCommandService;
        this.addressQueryService = addressQueryService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResource> createAddress(
            @PathVariable Long userId,
            @RequestBody CreateAddressResource resource) {

        var command = CreateAddressCommandFromResourceAssembler.toCommandFromResource(resource);
        var address = addressCommandService.handle(command, userId);
        var addressResource = AddressResourceFromEntityAssembler.toResourceFromEntity(address);

        return new ResponseEntity<>(addressResource, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AddressResource>> getAddressesByUserId(@PathVariable Long userId) {
        var query = new GetAddressesByUserIdQuery(userId);
        var addresses = addressQueryService.handle(query);
        var addressResources = addresses.stream()
                .map(AddressResourceFromEntityAssembler::toResourceFromEntity)
                .toList();

        return ResponseEntity.ok(addressResources);
    }

    @GetMapping("/default")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResource> getDefaultAddress(@PathVariable Long userId) {
        var query = new GetDefaultAddressByUserIdQuery(userId);
        var address = addressQueryService.handle(query);

        if (address.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var addressResource = AddressResourceFromEntityAssembler.toResourceFromEntity(address.get());
        return ResponseEntity.ok(addressResource);
    }


    @GetMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResource> getAddressById(
            @PathVariable Long userId,
            @PathVariable Long addressId) {

        var query = new GetAddressByIdQuery(addressId);
        var address = addressQueryService.handle(query);

        if (address.isEmpty()) {
            return ResponseEntity.notFound().build();
        }


        var addressResource = AddressResourceFromEntityAssembler.toResourceFromEntity(address.get());
        return ResponseEntity.ok(addressResource);
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResource> updateAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId,
            @RequestBody UpdateAddressResource resource) {

        var command = UpdateAddressCommandFromResourceAssembler.toCommandFromResource(addressId, resource);
        var address = addressCommandService.handle(command, userId);

        if (address.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var addressResource = AddressResourceFromEntityAssembler.toResourceFromEntity(address.get());
        return ResponseEntity.ok(addressResource);
    }

    @PatchMapping("/{addressId}/default")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResource> setDefaultAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {

        var command = new SetDefaultAddressCommand(addressId, userId);
        var address = addressCommandService.handle(command);

        if (address.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var addressResource = AddressResourceFromEntityAssembler.toResourceFromEntity(address.get());
        return ResponseEntity.ok(addressResource);
    }


    @DeleteMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long userId,
            @PathVariable Long addressId) {

        var command = new DeleteAddressCommand(addressId, userId);
        addressCommandService.handle(command);

        return ResponseEntity.noContent().build();
    }
}
