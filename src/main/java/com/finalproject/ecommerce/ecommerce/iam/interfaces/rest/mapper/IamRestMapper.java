package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.mapper;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.aggregates.User;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.*;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Address;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.entities.Role;

public class IamRestMapper {

    public record UserResource(Long id, String username, String email, String role) {
    }

    public record AuthenticatedUserResource(Long id, String username, String email, String accessToken, String refreshToken) {
    }

    public record AddressResource(Long id, Long userId, String street, String city, String state, String country, String postalCode, Boolean isDefault) {
    }

    public record SignUpResource(String username, String email, String password) {
    }

    public record SignInResource(String username, String password) {
    }

    public record RefreshTokenResource(String refreshToken) {
    }

    public record ForgotPasswordResource(String email) {
    }

    public record ResetPasswordResource(String token, String password, String passwordConfirmation) {
    }

    public record ActivateAccountResource(String activationToken) {
    }

    public record ResendActivationTokenResource(String email) {
    }

    public record UpdateUserResource(String username, String email, String password, String role) {
    }

    public record CreateAddressResource(String street, String city, String state, String country, String postalCode, Boolean isDefault) {
    }

    public record UpdateAddressResource(String street, String city, String state, String country, String postalCode) {
    }

    public static UserResource toResource(User user) {
        return new UserResource(user.getId(), user.getUsername(), user.getEmail(), user.getRole().getStringName());
    }

    public static AuthenticatedUserResource toAuthResource(User user, String accessToken, String refreshToken) {
        return new AuthenticatedUserResource(user.getId(), user.getUsername(), user.getEmail(), accessToken, refreshToken);
    }

    public static AddressResource toResource(Address address) {
        return new AddressResource(address.getId(), address.getUserId(), address.getStreet(), address.getCity(), address.getState(), address.getCountry(), address.getPostalCode(), address.getIsDefault());
    }

    public static SignUpCommand toSignUpCommand(SignUpResource r) {
        return new SignUpCommand(r.username(), r.email(), r.password());
    }

    public static SignInCommand toSignInCommand(SignInResource r) {
        return new SignInCommand(r.username(), r.password());
    }

    public static RefreshTokenCommand toRefreshCommand(RefreshTokenResource r) {
        return new RefreshTokenCommand(r.refreshToken());
    }

    public static UpdateUserCommand toUpdateCommand(Long userId, UpdateUserResource r) {
        Role role = r.role() != null ? Role.toRoleFromName(r.role()) : null;
        return new UpdateUserCommand(userId, r.username(), r.email(), r.password(), role);
    }

    public static CreateAddressCommand toCreateAddressCommand(CreateAddressResource r) {
        return new CreateAddressCommand(r.street(), r.city(), r.state(), r.country(), r.postalCode(), r.isDefault());
    }

    public static UpdateAddressCommand toUpdateAddressCommand(Long addressId, UpdateAddressResource r) {
        return new UpdateAddressCommand(addressId, r.street(), r.city(), r.state(), r.country(), r.postalCode());
    }
}

