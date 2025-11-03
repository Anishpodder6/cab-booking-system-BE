// src/main/java/com/cbs/CabBookingSystem.exception.handler/GlobalExceptionHandler.java
package com.cbs.CabBookingSystem.exception.handler;

import com.cbs.CabBookingSystem.exception.customexception.AlreadyRideAssignedException;
import com.cbs.CabBookingSystem.exception.customexception.RideNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException; // <-- NEW IMPORT
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handler for Validation Errors (Maps to 400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        // Simple error extraction for testing purposes
        ex.getBindingResult().getFieldErrors().forEach(error ->
        errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 2. Handler for Known Business Exceptions (Maps to 404 Not Found or other suitable status)
    @ExceptionHandler({RideNotFound.class, AlreadyRideAssignedException.class})
    public ResponseEntity<Map<String, String>> handleNotFoundExceptions(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 3. (Optional) Generic handler for all other exceptions (Maps to 500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllOtherExceptions(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "An unexpected error occurred: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}