package org.image.core.exception;

public class NotEnoughRightsException extends RuntimeException {
    private final String message;

    public NotEnoughRightsException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
