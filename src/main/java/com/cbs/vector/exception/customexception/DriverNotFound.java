package com.cbs.vector.exception.customexception;

import java.util.UUID;

public class DriverNotFound extends RuntimeException {

    public DriverNotFound(UUID userId) {
        super("Driver Not Found with ID: " + userId);
    }

    public DriverNotFound() {
        super("Driver Not Found");
    }
}
