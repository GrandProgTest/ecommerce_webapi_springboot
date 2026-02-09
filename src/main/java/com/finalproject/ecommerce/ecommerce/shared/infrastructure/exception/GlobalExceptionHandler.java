package com.finalproject.ecommerce.ecommerce.shared.infrastructure.exception;

import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.BusinessRuleException;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.InvalidOperationException;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.ResourceNotFoundException;
import com.finalproject.ecommerce.ecommerce.shared.infrastructure.exception.dtos.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiError> handleInvalidOperation(InvalidOperationException ex, HttpServletRequest request) {
        log.warn("Invalid operation at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        log.warn("Bad request at {}: {}", request.getRequestURI(), ex.getMessage());

        String message = ex instanceof MethodArgumentTypeMismatchException mismatch ? String.format("Parameter '%s' should be of type %s", mismatch.getName(), mismatch.getRequiredType() != null ? mismatch.getRequiredType().getSimpleName() : "unknown") : ex.getMessage() != null ? ex.getMessage() : "Invalid request parameters";

        return buildResponse(message, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRuleViolation(BusinessRuleException ex, HttpServletRequest request) {
        log.warn("Business rule violation at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return buildResponse("An unexpected error occurred. Please try again later or contact support if the problem persists.", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, org.springframework.http.HttpHeaders headers, org.springframework.http.HttpStatusCode status, WebRequest request) {

        log.warn("Invalid request body: {}", ex.getMessage());
        return buildObjectResponse("Invalid request body. Please check your JSON format and field types.", HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, org.springframework.http.HttpHeaders headers, org.springframework.http.HttpStatusCode status, WebRequest request) {

        String errors = ex.getBindingResult().getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errors);
        return buildObjectResponse(errors.isEmpty() ? "Validation failed" : errors, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, org.springframework.http.HttpHeaders headers, org.springframework.http.HttpStatusCode status, WebRequest request) {

        log.warn("Missing parameter: {}", ex.getParameterName());
        return buildObjectResponse(String.format("Required parameter '%s' is missing", ex.getParameterName()), HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, org.springframework.http.HttpHeaders headers, org.springframework.http.HttpStatusCode status, WebRequest request) {

        log.warn("Method not supported: {}", ex.getMethod());
        return buildObjectResponse(String.format("Method '%s' is not supported for this endpoint", ex.getMethod()), HttpStatus.METHOD_NOT_ALLOWED, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, org.springframework.http.HttpHeaders headers, org.springframework.http.HttpStatusCode status, WebRequest request) {

        log.warn("Resource not found: {}", request.getDescription(false));
        return buildObjectResponse("The requested resource was not found", HttpStatus.NOT_FOUND, request);
    }


    private ResponseEntity<ApiError> buildResponse(String message, HttpStatus status, HttpServletRequest request) {
        return buildResponse(message, status, request.getRequestURI());
    }

    private ResponseEntity<Object> buildObjectResponse(String message, HttpStatus status, WebRequest request) {
        String path = extractPathFromWebRequest(request);
        return ResponseEntity.status(status).body(buildApiError(message, status, path));
    }

    private ResponseEntity<ApiError> buildResponse(String message, HttpStatus status, String path) {
        return ResponseEntity.status(status).body(buildApiError(message, status, path));
    }

    private ApiError buildApiError(String message, HttpStatus status, String path) {
        return new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path);
    }

    private String extractPathFromWebRequest(WebRequest request) {
        String description = request.getDescription(false);
        return description.startsWith("uri=") ? description.substring(4) : description;
    }
}
