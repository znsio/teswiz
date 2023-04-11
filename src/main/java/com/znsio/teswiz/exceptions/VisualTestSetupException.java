package com.znsio.teswiz.exceptions;

public class VisualTestSetupException
        extends RuntimeException {
    public VisualTestSetupException(String message) {
        super(message);
    }

    public VisualTestSetupException(String message, Exception ex) {
        super(message, ex);
        ex.printStackTrace();
    }
}