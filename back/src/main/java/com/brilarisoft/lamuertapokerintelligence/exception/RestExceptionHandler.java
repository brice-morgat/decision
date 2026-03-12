package com.brilarisoft.lamuertapokerintelligence.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessValidation(BusinessValidationException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_VALIDATION_ERROR", exception.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<ApiErrorDetail> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldMessage)
                .toList();

        List<ApiErrorDetail> globalDetails = exception.getBindingResult()
                .getGlobalErrors()
                .stream()
                .map(this::toGlobalMessage)
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "REQUEST_VALIDATION_ERROR",
                "Request validation failed",
                request,
                java.util.stream.Stream.concat(details.stream(), globalDetails.stream()).toList()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        List<ApiErrorDetail> details = exception.getConstraintViolations()
                .stream()
                .map(violation -> new ApiErrorDetail(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "CONSTRAINT_VALIDATION_ERROR", "Constraint validation failed", request, details);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestParam(MissingServletRequestParameterException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "REQUEST_VALIDATION_ERROR",
                "Missing required request parameter",
                request,
                List.of(new ApiErrorDetail(exception.getParameterName(), "Parameter is required"))
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        String field = exception.getName() == null ? "unknown" : exception.getName();
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "REQUEST_VALIDATION_ERROR",
                "Request parameter type mismatch",
                request,
                List.of(new ApiErrorDetail(field, exception.getMostSpecificCause().getMessage()))
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Unexpected server error",
                request,
                List.of(new ApiErrorDetail("exception", exception.getMessage()))
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            List<ApiErrorDetail> details
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                request.getMethod(),
                details
        );
        return ResponseEntity.status(status).body(response);
    }

    private ApiErrorDetail toFieldMessage(FieldError fieldError) {
        return new ApiErrorDetail(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private ApiErrorDetail toGlobalMessage(ObjectError error) {
        return new ApiErrorDetail(error.getObjectName(), error.getDefaultMessage());
    }
}
