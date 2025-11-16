package com.cbs.vector.exception.customexception;

public class RideNotFound extends RuntimeException {

    public RideNotFound(String message) {
        super(message);
    }

    public RideNotFound() {
        super("Ride Not Found");
    }
}
