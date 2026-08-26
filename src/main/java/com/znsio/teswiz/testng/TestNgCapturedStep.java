package com.znsio.teswiz.testng;

public record TestNgCapturedStep(String stepName, String status, long durationNanos) {
    public static final String PASSED = "passed";
    public static final String FAILED = "failed";
}
