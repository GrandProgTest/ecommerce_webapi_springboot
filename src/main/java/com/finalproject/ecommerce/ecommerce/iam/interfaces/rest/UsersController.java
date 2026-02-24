package com.finalproject.ecommerce.ecommerce.iam.interfaces.rest;

import com.finalproject.ecommerce.ecommerce.iam.domain.model.commands.DeleteUserCommand;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetAllUsersQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.model.queries.GetUserByIdQuery;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserCommandService;
import com.finalproject.ecommerce.ecommerce.iam.domain.services.UserQueryService;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.mapper.IamRestMapper;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.mapper.IamRestMapper.UpdateUserResource;
import com.finalproject.ecommerce.ecommerce.iam.interfaces.rest.mapper.IamRestMapper.UserResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Available User Endpoints")
public class UsersController {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public UsersController(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }


    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var users = userQueryService.handle(new GetAllUsersQuery());
        return ResponseEntity.ok(users.stream().map(IamRestMapper::toResource).toList());
    }


    @GetMapping("/{userId}")
    @Operation(summary = "Get user by id")
    public ResponseEntity<UserResource> getUserById(@PathVariable Long userId) {
        var user = userQueryService.handle(new GetUserByIdQuery(userId));
        if (user.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(IamRestMapper.toResource(user.get()));
    }


    @PutMapping("/{userId}")
    @Operation(summary = "Update user")
    public ResponseEntity<UserResource> updateUser(@PathVariable Long userId, @RequestBody UpdateUserResource resource) {
        var command = IamRestMapper.toUpdateCommand(userId, resource);
        var updatedUser = userCommandService.handle(command);
        if (updatedUser.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(IamRestMapper.toResource(updatedUser.get()));
    }


    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userCommandService.handle(new DeleteUserCommand(userId));
        return ResponseEntity.ok("User with id " + userId + " successfully deleted");
    }
}