package com.znsio.sample.e2e.exceptions.jiomeet;

public class InAMeetingException
        extends RuntimeException {
    public InAMeetingException(String message) {
        super(message);
    }

    public InAMeetingException(String message, Exception e) {
        super(message, e);
    }
}
