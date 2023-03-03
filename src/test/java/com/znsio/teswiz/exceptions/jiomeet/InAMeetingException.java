package com.znsio.teswiz.exceptions.jiomeet;

public class InAMeetingException
        extends RuntimeException {
    public InAMeetingException(String message) {
        super(message);
    }

    public InAMeetingException(String message, Exception e) {
        super(message, e);
    }
}
