package com.znsio.e2e.exceptions;

public class TestExecutionFailedException
        extends RuntimeException {
    public TestExecutionFailedException(String message) {
        super(message);
    }

    public TestExecutionFailedException(String message, Exception ex) {
        super(message, ex);
    }
}
