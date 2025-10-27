package com.cbs.CabBookingSystem.exception.handler;

import com.cbs.CabBookingSystem.exception.customexception.AlreadyRideAssignedException;
import com.cbs.CabBookingSystem.exception.customexception.RideNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleRideNotFoundException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

//    @ExceptionHandler(AlreadyRideAssignedException.class)
//    public ResponseEntity<Map<String, String>> handleRideNotFoundException(AlreadyRideAssignedException ex) {
//        Map<String, String> error = new HashMap<>();
//        error.put("error", ex.getMessage());
//
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
//    }

}
