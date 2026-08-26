package com.znsio.teswiz.testng;

import java.util.ArrayList;
import java.util.List;

// Records steps in the order they START (not the order they return), so the
// generated report reflects the real call sequence and nesting depth (test -> BL
// -> screen -> ...) rather than a post-order completion trace. beginStep() reserves
// a slot and increments the current thread's call depth; endStep() fills in the
// outcome for that slot and restores the depth for sibling calls.
public final class TestNgStepRecorder {
    private static final ThreadLocal<List<PendingStep>> CAPTURED_STEPS = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<Integer> CALL_DEPTH = ThreadLocal.withInitial(() -> 0);

    private TestNgStepRecorder() { }

    public static void startCapturingStepsForCurrentThread() {
        CAPTURED_STEPS.get().clear();
        CALL_DEPTH.set(0);
    }

    public static int beginStep(String stepName, String matchLocation) {
        List<PendingStep> steps = CAPTURED_STEPS.get();
        int depth = CALL_DEPTH.get();
        steps.add(new PendingStep(stepName, matchLocation, depth));
        CALL_DEPTH.set(depth + 1);
        return steps.size() - 1;
    }

    public static void endStep(int stepIndex, String status, long durationNanos) {
        CAPTURED_STEPS.get().get(stepIndex).complete(status, durationNanos);
        CALL_DEPTH.set(CALL_DEPTH.get() - 1);
    }

    public static List<TestNgCapturedStep> stopCapturingAndGetStepsForCurrentThread() {
        List<TestNgCapturedStep> steps = CAPTURED_STEPS.get().stream().map(PendingStep::toImmutable).toList();
        CAPTURED_STEPS.get().clear();
        CALL_DEPTH.set(0);
        return steps;
    }

    private static final class PendingStep {
        private final String stepName;
        private final String matchLocation;
        private final int depth;
        private String status;
        private long durationNanos;

        private PendingStep(String stepName, String matchLocation, int depth) {
            this.stepName = stepName;
            this.matchLocation = matchLocation;
            this.depth = depth;
        }

        private void complete(String status, long durationNanos) {
            this.status = status;
            this.durationNanos = durationNanos;
        }

        private TestNgCapturedStep toImmutable() {
            return new TestNgCapturedStep(stepName, matchLocation, depth, status, durationNanos);
        }
    }
}
