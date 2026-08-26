package com.znsio.teswiz.testng;

import java.util.ArrayList;
import java.util.List;

public final class TestNgStepRecorder {
    private static final ThreadLocal<List<TestNgCapturedStep>> CAPTURED_STEPS = ThreadLocal.withInitial(ArrayList::new);

    private TestNgStepRecorder() { }

    public static void startCapturingStepsForCurrentThread() {
        CAPTURED_STEPS.get().clear();
    }

    public static void recordStep(String stepName, String status, long durationNanos) {
        CAPTURED_STEPS.get().add(new TestNgCapturedStep(stepName, status, durationNanos));
    }

    public static List<TestNgCapturedStep> stopCapturingAndGetStepsForCurrentThread() {
        List<TestNgCapturedStep> steps = new ArrayList<>(CAPTURED_STEPS.get());
        CAPTURED_STEPS.get().clear();
        return steps;
    }
}
