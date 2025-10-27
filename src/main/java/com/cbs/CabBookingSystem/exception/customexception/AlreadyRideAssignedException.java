package com.cbs.CabBookingSystem.exception.customexception;

public class AlreadyRideAssignedException extends RuntimeException {

    public AlreadyRideAssignedException(String message) {
        super(message);
    }
}
