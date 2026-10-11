package com.digitalharbor.duckstore.common;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException e) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> conflict(ConflictException e) {
        return respond(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e) {
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).sorted().toList();
        return ResponseEntity.badRequest().body(new ApiError(400, "Validation failed", details));
    }

   
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException e) {
        String cause = NestedExceptionUtils.getMostSpecificCause(e).getMessage();
        return respond(HttpStatus.BAD_REQUEST, "Invalid request body: " + cause);
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> badArgument(Exception e) {
        return respond(HttpStatus.BAD_REQUEST, "Invalid request: " + e.getMessage());
    }

   
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integrity(DataIntegrityViolationException e) {
        log.warn("Data integrity violation", e);
        return respond(HttpStatus.CONFLICT, "A duck with this color, size and price already exists");
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    ResponseEntity<ApiError> busy(ConcurrencyFailureException e) {
        log.warn("Concurrency failure", e);
        return respond(HttpStatus.CONFLICT, "The duck was modified by another request, please retry");
    }

    @ExceptionHandler(ArithmeticException.class)
    ResponseEntity<ApiError> overflow(ArithmeticException e) {
        return respond(HttpStatus.CONFLICT, "Resulting quantity is too large");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception e) {
        if (e instanceof ErrorResponse standard) { // 404 unknown URL, 405 wrong method, 415 media type...
            return respond(HttpStatus.valueOf(standard.getStatusCode().value()), standard.getBody().getDetail());
        }
        log.error("Unexpected error", e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error");
    }

    private ResponseEntity<ApiError> respond(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), message));
    }
}
