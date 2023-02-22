package com.znsio.e2e.exceptions;

public class FileNotUploadedException
        extends RuntimeException {
    public FileNotUploadedException(String message) {
        super(message);
    }

    public FileNotUploadedException(String message, Exception ex) {
        super(message, ex);
    }
}
