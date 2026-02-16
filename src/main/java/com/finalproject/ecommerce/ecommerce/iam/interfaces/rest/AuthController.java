package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ActivateAccountCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ForgotPasswordCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ResendActivationTokenCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ResetPasswordCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.RefreshTokenCommandService;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserCommandService;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignOutCommand;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.*;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.RefreshTokenCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.RateLimitExceededException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    @Operation(summary = "Sign up", description = "Register a new user. Role is automatically assigned based on email domain: @ravn.co = MANAGER, others = CLIENT. Account activation required via email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully. Check email for activation link."),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    public ResponseEntity<UserResource> signUp(@Valid @RequestBody SignUpResource resource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = userCommandService.handle(signUpCommand);

        if (result.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(result.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResource);
    }

    @GetMapping("/activate")
    @Operation(summary = "Activate account via URL", description = "Activate user account using token from email link")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account activated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired activation token")
    })
    public ResponseEntity<String> activateAccountViaUrl(@RequestParam String token) {
        var command = new ActivateAccountCommand(token);
        boolean success = userCommandService.handle(command);

        if (success) {
            return ResponseEntity.ok("Account activated successfully. You can now sign in.");
        }
        return ResponseEntity.badRequest().body("Failed to activate account");
    }

    @PostMapping("/resend-activation")
    @Operation(summary = "Resend activation token", description = "Resend activation email to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Activation email sent successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Account already activated")
    })
    public ResponseEntity<String> resendActivationToken(@Valid @RequestBody ResendActivationTokenResource resource) {
        var command = new ResendActivationTokenCommand(resource.email());
        boolean success = userCommandService.handle(command);

        if (success) {
            return ResponseEntity.ok("Activation email sent. Please check your inbox.");
        }
        return ResponseEntity.badRequest().body("Failed to resend activation email");
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


    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Request password reset token via email (Rate limited: 10 requests per hour)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "429", description = "Too many requests - Rate limit exceeded")
    })
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordResource resource) {
        try {
            var command = new ForgotPasswordCommand(resource.email());
            boolean success = userCommandService.handle(command);

            if (success) {
                return ResponseEntity.ok("Password reset token sent to your email. Token expires in 15 minutes.");
            }
            return ResponseEntity.badRequest().body("Failed to send password reset email");
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(429).body(e.getMessage() + " Try again in " + (e.getRetryAfterSeconds() / 60) + " minutes.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset password using the token received via email (Rate limited: 10 requests per hour)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid token or passwords don't match"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired reset token"),
            @ApiResponse(responseCode = "429", description = "Too many requests - Rate limit exceeded")
    })
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordResource resource) {
        try {
            var command = new ResetPasswordCommand(
                    resource.token(),
                    resource.password(),
                    resource.passwordConfirmation()
            );
            boolean success = userCommandService.handle(command);

            if (success) {
                return ResponseEntity.ok("Password reset successfully. You can now sign in with your new password.");
            }
            return ResponseEntity.badRequest().body("Failed to reset password");
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(429).body(e.getMessage() + " Try again in " + (e.getRetryAfterSeconds() / 60) + " minutes.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
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

