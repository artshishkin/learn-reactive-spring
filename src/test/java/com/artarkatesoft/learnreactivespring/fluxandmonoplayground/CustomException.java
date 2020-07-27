package com.artarkatesoft.learnreactivespring.fluxandmonoplayground;

public class CustomException extends Throwable {

    private final String message;

    public CustomException(Throwable cause) {
        super(cause);
        this.message = cause.getMessage();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
