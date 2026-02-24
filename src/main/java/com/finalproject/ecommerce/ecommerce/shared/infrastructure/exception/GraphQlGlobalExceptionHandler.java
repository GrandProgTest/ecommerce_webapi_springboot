package com.finalproject.ecommerce.ecommerce.shared.infrastructure.exception;

import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.BusinessRuleException;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.InvalidOperationException;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.ResourceNotFoundException;
import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.Map;

@ControllerAdvice
public class GraphQlGlobalExceptionHandler {

    private enum GraphQlErrorType implements ErrorClassification {
        NOT_FOUND,
        BAD_REQUEST,
        FORBIDDEN,
        CONFLICT,
        INTERNAL_ERROR
    }


    @GraphQlExceptionHandler(ResourceNotFoundException.class)
    public GraphQLError handleResourceNotFound(ResourceNotFoundException ex, DataFetchingEnvironment env) {
        return buildError(ex.getMessage(), GraphQlErrorType.NOT_FOUND, env, "resource-not-found");
    }

    @GraphQlExceptionHandler(InvalidOperationException.class)
    public GraphQLError handleInvalidOperation(InvalidOperationException ex, DataFetchingEnvironment env) {
        return buildError(ex.getMessage(), GraphQlErrorType.BAD_REQUEST, env, "invalid-operation");
    }

    @GraphQlExceptionHandler(BusinessRuleException.class)
    public GraphQLError handleBusinessRuleViolation(BusinessRuleException ex, DataFetchingEnvironment env) {
        return buildError(ex.getMessage(), GraphQlErrorType.CONFLICT, env, "business-rule-violation");
    }


    @GraphQlExceptionHandler(IllegalArgumentException.class)
    public GraphQLError handleIllegalArgument(IllegalArgumentException ex, DataFetchingEnvironment env) {
        return buildError(
                ex.getMessage() != null ? ex.getMessage() : "Invalid request parameters",
                GraphQlErrorType.BAD_REQUEST, env, "bad-request"
        );
    }

    @GraphQlExceptionHandler(NumberFormatException.class)
    public GraphQLError handleNumberFormat(NumberFormatException ex, DataFetchingEnvironment env) {
        return buildError("Invalid numeric value: " + ex.getMessage(), GraphQlErrorType.BAD_REQUEST, env, "bad-request");
    }

    @GraphQlExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public GraphQLError handleAccessDenied(Exception ex, DataFetchingEnvironment env) {
        return buildError(
                "Access denied. You don't have permission to perform this operation.",
                GraphQlErrorType.FORBIDDEN, env, "access-denied"
        );
    }

    @GraphQlExceptionHandler(Exception.class)
    public GraphQLError handleGenericException(Exception ex, DataFetchingEnvironment env) {
        return buildError(
                "An unexpected error occurred. Please try again later.",
                GraphQlErrorType.INTERNAL_ERROR, env, "internal-error"
        );
    }


    private GraphQLError buildError(String message, GraphQlErrorType errorType,
                                    DataFetchingEnvironment env, String errorCode) {
        return GraphQLError.newError()
                .message(message)
                .errorType(errorType)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .extensions(Map.of(
                        "errorCode", errorCode,
                        "classification", errorType.name()
                ))
                .build();
    }
}

