package com.finalproject.ecommerce.ecommerce.shared.infrastructure.exception;

import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.InvalidImageTypeException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.MaximumImagesExceededException;
import com.finalproject.ecommerce.ecommerce.products.domain.exceptions.ProductImageNotFoundException;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.BusinessRuleException;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.InvalidOperationException;
import com.finalproject.ecommerce.ecommerce.shared.domain.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request, "resource-not-found");
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ProblemDetail> handleInvalidOperation(InvalidOperationException ex, HttpServletRequest request) {
        log.warn("Invalid operation at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request, "invalid-operation");
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ProblemDetail> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        log.warn("Bad request at {}: {}", request.getRequestURI(), ex.getMessage());

        String message = ex instanceof MethodArgumentTypeMismatchException mismatch ? String.format("Parameter '%s' should be of type %s", mismatch.getName(), mismatch.getRequiredType() != null ? mismatch.getRequiredType().getSimpleName() : "unknown") : ex.getMessage() != null ? ex.getMessage() : "Invalid request parameters";

        return buildResponse(message, HttpStatus.BAD_REQUEST, request, "bad-request");
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRuleViolation(BusinessRuleException ex, HttpServletRequest request) {
        log.warn("Business rule violation at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, request, "business-rule-violation");
    }

    @ExceptionHandler(InvalidImageTypeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidImageType(InvalidImageTypeException ex, HttpServletRequest request) {
        log.warn("Invalid image type at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request, "invalid-image-type");
    }

    @ExceptionHandler(MaximumImagesExceededException.class)
    public ResponseEntity<ProblemDetail> handleMaximumImagesExceeded(MaximumImagesExceededException ex, HttpServletRequest request) {
        log.warn("Maximum images exceeded at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request, "maximum-images-exceeded");
    }

    @ExceptionHandler(ProductImageNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleProductImageNotFound(ProductImageNotFoundException ex, HttpServletRequest request) {
        log.warn("Product image not found at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request, "product-image-not-found");
    }

    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ProblemDetail> handleAccessDenied(RuntimeException ex, HttpServletRequest request) {
        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                "Access denied. You don't have permission to access this resource.",
                HttpStatus.FORBIDDEN,
                request,
                "access-denied"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return buildResponse("An unexpected error occurred. Please try again later or contact support if the problem persists.", HttpStatus.INTERNAL_SERVER_ERROR, request, "internal-server-error");
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, org.springframework.http.HttpHeaders headers, org.springframework.http.HttpStatusCode status, WebRequest request) {

        log.warn("Invalid request body: {}", ex.getMessage());
        return buildObjectResponse("Invalid request body. Please check your JSON format and field types.", HttpStatus.BAD_REQUEST, request, "invalid-request-body");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, org.springframework.http.HttpHeaders headers, org.springframework.http.HttpStatusCode status, WebRequest request) {

        String errors = ex.getBindingResult().getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errors);
        return buildObjectResponse(errors.isEmpty() ? "Validation failed" : errors, HttpStatus.BAD_REQUEST, request, "validation-failed");
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, org.springframework.http.HttpHeaders headers, org.springframework.http.HttpStatusCode status, WebRequest request) {

        log.warn("Missing parameter: {}", ex.getParameterName());
        return buildObjectResponse(String.format("Required parameter '%s' is missing", ex.getParameterName()), HttpStatus.BAD_REQUEST, request, "missing-parameter");
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, org.springframework.http.HttpHeaders headers, org.springframework.http.HttpStatusCode status, WebRequest request) {

        log.warn("Method not supported: {}", ex.getMethod());
        return buildObjectResponse(String.format("Method '%s' is not supported for this endpoint", ex.getMethod()), HttpStatus.METHOD_NOT_ALLOWED, request, "method-not-allowed");
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex, org.springframework.http.HttpHeaders headers, org.springframework.http.HttpStatusCode status, WebRequest request) {

        log.warn("Resource not found: {}", request.getDescription(false));
        return buildObjectResponse("The requested resource was not found", HttpStatus.NOT_FOUND, request, "endpoint-not-found");
    }


    private ResponseEntity<ProblemDetail> buildResponse(String message, HttpStatus status, HttpServletRequest request, String typeCode) {
        return buildResponse(message, status, request.getRequestURI(), typeCode);
    }

    private ResponseEntity<Object> buildObjectResponse(String message, HttpStatus status, WebRequest request, String typeCode) {
        String path = extractPathFromWebRequest(request);
        return ResponseEntity.status(status).body(buildProblemDetail(message, status, path, typeCode));
    }

    private ResponseEntity<ProblemDetail> buildResponse(String message, HttpStatus status, String path, String typeCode) {
        return ResponseEntity.status(status).body(buildProblemDetail(message, status, path, typeCode));
    }

    // Problem Details RFC 7807: https://datatracker.ietf.org/doc/html/rfc7807
    private ProblemDetail buildProblemDetail(String message, HttpStatus status, String path, String typeCode) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
        problemDetail.setType(URI.create("https://api.ecommerce.com/errors/" + typeCode));
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setInstance(URI.create(path));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    private String extractPathFromWebRequest(WebRequest request) {
        String description = request.getDescription(false);
        return description.startsWith("uri=") ? description.substring(4) : description;
    }
}
