package com.znsio.teswiz.exceptions;

public class FileNotUploadedException
        extends RuntimeException {
    public FileNotUploadedException(String message) {
        super(message);
    }

    public FileNotUploadedException(String message, Exception ex) {
        super(message, ex);
    }
}
