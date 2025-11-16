package com.cbs.vector.exception.customexception;

import java.util.UUID;

public class UserNotFound extends RuntimeException {

    public UserNotFound(UUID userId) {
        super("User Not Found with ID: " + userId);
    }

    public UserNotFound() {
        super("User Not Found");
    }
}
