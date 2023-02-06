package com.znsio.e2e.exceptions;

public class VisualTestSetupException extends RuntimeException{
    public VisualTestSetupException(String message) {
        super(message);
    }
    public VisualTestSetupException(String message, Exception ex) {
        super(message, ex);
    }
}