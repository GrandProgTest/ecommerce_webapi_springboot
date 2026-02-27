package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.DeleteAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SetDefaultAddressCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetAddressByIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetAddressesByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetDefaultAddressByUserIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.AddressCommandService;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.AddressQueryService;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.mapper.IamRestMapper;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.mapper.IamRestMapper.AddressResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.mapper.IamRestMapper.CreateAddressResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.mapper.IamRestMapper.UpdateAddressResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/users/{userId}/addresses", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User Addresses", description = "Address management endpoints")
public class AddressesController {

    private final AddressCommandService addressCommandService;
    private final AddressQueryService addressQueryService;

    public AddressesController(AddressCommandService addressCommandService, AddressQueryService addressQueryService) {
        this.addressCommandService = addressCommandService;
        this.addressQueryService = addressQueryService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResource> createAddress(@PathVariable Long userId, @RequestBody CreateAddressResource resource) {
        var command = IamRestMapper.toCreateAddressCommand(resource);
        var address = addressCommandService.handle(command, userId);
        return new ResponseEntity<>(IamRestMapper.toResource(address), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AddressResource>> getAddressesByUserId(@PathVariable Long userId) {
        var addresses = addressQueryService.handle(new GetAddressesByUserIdQuery(userId));
        return ResponseEntity.ok(addresses.stream().map(IamRestMapper::toResource).toList());
    }

    @GetMapping("/default")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResource> getDefaultAddress(@PathVariable Long userId) {
        var address = addressQueryService.handle(new GetDefaultAddressByUserIdQuery(userId));
        if (address.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(IamRestMapper.toResource(address.get()));
    }

    @GetMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResource> getAddressById(@PathVariable Long userId, @PathVariable Long addressId) {
        var address = addressQueryService.handle(new GetAddressByIdQuery(addressId));
        if (address.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(IamRestMapper.toResource(address.get()));
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResource> updateAddress(@PathVariable Long userId, @PathVariable Long addressId, @RequestBody UpdateAddressResource resource) {
        var command = IamRestMapper.toUpdateAddressCommand(addressId, resource);
        var address = addressCommandService.handle(command, userId);
        if (address.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(IamRestMapper.toResource(address.get()));
    }

    @PatchMapping("/{addressId}/default")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResource> setDefaultAddress(@PathVariable Long userId, @PathVariable Long addressId) {
        var address = addressCommandService.handle(new SetDefaultAddressCommand(addressId, userId));
        if (address.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(IamRestMapper.toResource(address.get()));
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long userId, @PathVariable Long addressId) {
        addressCommandService.handle(new DeleteAddressCommand(addressId, userId));
        return ResponseEntity.noContent().build();
    }
}
