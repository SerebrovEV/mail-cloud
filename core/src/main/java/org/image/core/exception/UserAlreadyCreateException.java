package org.image.core.exception;

public class UserAlreadyCreateException extends RuntimeException {
    private final String message;

    public UserAlreadyCreateException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
