package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.DeleteUserCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetAllUsersQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetUserByIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserCommandService;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserQueryService;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.SignInResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.SignUpResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.UpdateUserResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.resources.UserResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.UpdateUserCommandFromResourceAssembler;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/users", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Available User Endpoints")
public class UsersController {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public UsersController(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }

    @PostMapping("/sign-up")
    @Operation(summary = "Sign up a new user", description = "Sign up a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource resource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
        var userId = userCommandService.handle(signUpCommand);
        if (userId == null || userId == 0L) return ResponseEntity.badRequest().build();
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var user = userQueryService.handle(getUserByIdQuery);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        var userEntity = user.get();
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(userEntity);
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Sign in a user", description = "Sign in a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials")})
    public ResponseEntity<UserResource> signIn(@RequestBody SignInResource resource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
        var user = userCommandService.handle(signInCommand);
        if (user.isEmpty()) return ResponseEntity.badRequest().build();
        var userEntity = user.get();
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(userEntity);
        return ResponseEntity.ok(userResource);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by id", description = "Get user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public ResponseEntity<UserResource> getUserById(@PathVariable Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var user = userQueryService.handle(getUserByIdQuery);
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        var userEntity = user.get();
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(userEntity);
        return ResponseEntity.ok(userResource);
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Get all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found"),
            @ApiResponse(responseCode = "404", description = "Users not found")})
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var users = userQueryService.handle(new GetAllUsersQuery());
        if (users.isEmpty()) return ResponseEntity.notFound().build();
        var userResources = users.stream()
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(userResources);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Update user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public ResponseEntity<UserResource> updateUser(@PathVariable Long userId, @RequestBody UpdateUserResource resource) {
        var updateUserCommand = UpdateUserCommandFromResourceAssembler.toCommandFromResource(userId, resource);
        var updatedUser = userCommandService.handle(updateUserCommand);
        if (updatedUser.isEmpty()) return ResponseEntity.notFound().build();
        var updatedUserEntity = updatedUser.get();
        var updatedUserResource = UserResourceFromEntityAssembler.toResourceFromEntity(updatedUserEntity);
        return ResponseEntity.ok(updatedUserResource);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Delete user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        var deleteUserCommand = new DeleteUserCommand(userId);
        userCommandService.handle(deleteUserCommand);
        return ResponseEntity.ok("User with given id successfully deleted");
    }
}
