package org.image.core.exception;

public class IncorrectFormatEmailException extends RuntimeException {

    private final String message;

    public IncorrectFormatEmailException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
