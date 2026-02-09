package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.iam.domain.services.RefreshTokenCommandService;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserCommandService;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignOutCommand;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.RefreshTokenResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.SignInResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.SignUpResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.UserResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.RefreshTokenCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication Endpoints")
public class AuthController {

    private final UserCommandService userCommandService;
    private final RefreshTokenCommandService refreshTokenCommandService;

    public AuthController(
            UserCommandService userCommandService,
            RefreshTokenCommandService refreshTokenCommandService) {
        this.userCommandService = userCommandService;
        this.refreshTokenCommandService = refreshTokenCommandService;
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Sign in", description = "Authenticate user and return access + refresh tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource resource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = userCommandService.handle(signInCommand);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var userAndAccessToken = result.get().getLeft();
        var user = userAndAccessToken.getLeft();
        var accessToken = userAndAccessToken.getRight();
        var refreshToken = result.get().getRight();

        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler
                .toResourceFromEntity(user, accessToken, refreshToken);

        return ResponseEntity.ok(authenticatedUserResource);
    }

    @PostMapping("/sign-up")
    @Operation(summary = "Sign up", description = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource resource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = userCommandService.handle(signUpCommand);

        if (result.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(result.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResource);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token (with rotation)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
            @ApiResponse(responseCode = "403", description = "Token reuse detected - security breach")
    })
    public ResponseEntity<AuthenticatedUserResource> refresh(@RequestBody RefreshTokenResource resource) {
        try {
            var refreshCommand = RefreshTokenCommandFromResourceAssembler.toCommandFromResource(resource);
            var result = refreshTokenCommandService.handle(refreshCommand);

            if (result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            var userAndAccessToken = result.get().getLeft();
            var user = userAndAccessToken.getLeft();
            var newAccessToken = userAndAccessToken.getRight();
            var newRefreshToken = result.get().getRight();

            var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler
                    .toResourceFromEntity(user, newAccessToken, newRefreshToken);

            return ResponseEntity.ok(authenticatedUserResource);

        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }


    @PostMapping("/sign-out")
    @Operation(summary = "Sign-Out", description = "Revoke all refresh tokens for authenticated user (logout)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<String> signOut() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }

        String username = authentication.getName();
        var signOutCommand = new SignOutCommand(username);
        refreshTokenCommandService.handle(signOutCommand);

        return ResponseEntity.ok("Logged out successfully");
    }
}

