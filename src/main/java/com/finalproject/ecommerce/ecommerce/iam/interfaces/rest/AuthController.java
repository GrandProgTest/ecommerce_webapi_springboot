package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserCommandService;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.SignInResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.SignUpResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.UserResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication Endpoints")
public class AuthController {

    private final UserCommandService userCommandService;

    public AuthController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }


    @PostMapping("/sign-in")
    @Operation(summary = "Sign in", description = "Authenticate user and return JWT token")
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource resource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = userCommandService.handle(signInCommand);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var user = result.get().getLeft();
        var token = result.get().getRight();
        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(user, token);

        return ResponseEntity.ok(authenticatedUserResource);
    }


    @PostMapping("/sign-up")
    @Operation(summary = "Sign up", description = "Register a new user")
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource resource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = userCommandService.handle(signUpCommand);

        if (result.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(result.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResource);
    }
}
