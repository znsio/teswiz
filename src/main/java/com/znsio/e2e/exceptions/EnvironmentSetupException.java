package com.znsio.e2e.exceptions;

public class EnvironmentSetupException
        extends RuntimeException {
    public EnvironmentSetupException(String message) {
        super(message);
    }

    public EnvironmentSetupException(String message, Exception ex) {
        super(message, ex);
    }
}
