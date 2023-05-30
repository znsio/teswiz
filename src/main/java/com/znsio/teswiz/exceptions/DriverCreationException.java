package com.znsio.teswiz.exceptions;

public class DriverCreationException
        extends RuntimeException {
    public DriverCreationException(String message) {
        super(message);
    }

    public DriverCreationException(String message, Exception ex) {
        super(message, ex);
    }
}
