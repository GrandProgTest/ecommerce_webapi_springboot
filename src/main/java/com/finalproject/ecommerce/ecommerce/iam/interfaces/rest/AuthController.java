package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ActivateAccountCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ForgotPasswordCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ResendActivationTokenCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.ResetPasswordCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.RefreshTokenCommandService;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserCommandService;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.SignOutCommand;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.mapper.IamRestMapper;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.mapper.IamRestMapper.*;
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

    public AuthController(UserCommandService userCommandService, RefreshTokenCommandService refreshTokenCommandService) {
        this.userCommandService = userCommandService;
        this.refreshTokenCommandService = refreshTokenCommandService;
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Sign in")
    @ApiResponses(value = { @ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404"), @ApiResponse(responseCode = "401") })
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource resource) {
        var command = IamRestMapper.toSignInCommand(resource);
        var result = userCommandService.handle(command);
        if (result.isEmpty()) return ResponseEntity.notFound().build();
        var userAndAccessToken = result.get().getLeft();
        var user = userAndAccessToken.getLeft();
        var accessToken = userAndAccessToken.getRight();
        var refreshToken = result.get().getRight();
        return ResponseEntity.ok(IamRestMapper.toAuthResource(user, accessToken, refreshToken));
    }

    @PostMapping("/sign-up")
    @Operation(summary = "Sign up")
    @ApiResponses(value = { @ApiResponse(responseCode = "201"), @ApiResponse(responseCode = "400") })
    public ResponseEntity<UserResource> signUp(@Valid @RequestBody SignUpResource resource) {
        var command = IamRestMapper.toSignUpCommand(resource);
        var result = userCommandService.handle(command);
        if (result.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.status(HttpStatus.CREATED).body(IamRestMapper.toResource(result.get()));
    }

    @GetMapping("/activate")
    @Operation(summary = "Activate account via URL")
    public ResponseEntity<String> activateAccountViaUrl(@RequestParam String token) {
        boolean success = userCommandService.handle(new ActivateAccountCommand(token));
        return success ? ResponseEntity.ok("Account activated successfully. You can now sign in.") : ResponseEntity.badRequest().body("Failed to activate account");
    }

    @PostMapping("/resend-activation")
    @Operation(summary = "Resend activation token")
    public ResponseEntity<String> resendActivationToken(@Valid @RequestBody ResendActivationTokenResource resource) {
        boolean success = userCommandService.handle(new ResendActivationTokenCommand(resource.email()));
        return success ? ResponseEntity.ok("Activation email sent. Please check your inbox.") : ResponseEntity.badRequest().body("Failed to resend activation email");
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token")
    @ApiResponses(value = { @ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "401"), @ApiResponse(responseCode = "403") })
    public ResponseEntity<AuthenticatedUserResource> refresh(@RequestBody RefreshTokenResource resource) {
        try {
            var command = IamRestMapper.toRefreshCommand(resource);
            var result = refreshTokenCommandService.handle(command);
            if (result.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            var userAndAccessToken = result.get().getLeft();
            var user = userAndAccessToken.getLeft();
            var newAccessToken = userAndAccessToken.getRight();
            var newRefreshToken = result.get().getRight();
            return ResponseEntity.ok(IamRestMapper.toAuthResource(user, newAccessToken, newRefreshToken));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordResource resource) {
        try {
            boolean success = userCommandService.handle(new ForgotPasswordCommand(resource.email()));
            return success ? ResponseEntity.ok("Password reset token sent to your email. Token expires in 15 minutes.") : ResponseEntity.badRequest().body("Failed to send password reset email");
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(429).body(e.getMessage() + " Try again in " + (e.getRetryAfterSeconds() / 60) + " minutes.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordResource resource) {
        try {
            boolean success = userCommandService.handle(new ResetPasswordCommand(resource.token(), resource.password(), resource.passwordConfirmation()));
            return success ? ResponseEntity.ok("Password reset successfully. You can now sign in with your new password.") : ResponseEntity.badRequest().body("Failed to reset password");
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(429).body(e.getMessage() + " Try again in " + (e.getRetryAfterSeconds() / 60) + " minutes.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/sign-out")
    @Operation(summary = "Sign-Out")
    public ResponseEntity<String> signOut() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        refreshTokenCommandService.handle(new SignOutCommand(authentication.getName()));
        return ResponseEntity.ok("Logged out successfully");
    }
}
