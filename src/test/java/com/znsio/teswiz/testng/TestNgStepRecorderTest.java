package com.znsio.teswiz.testng;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestNgStepRecorderTest {

    @Test
    void shouldReturnRecordedStepsForTheCurrentThreadAndClearAfterwards() {
        TestNgStepRecorder.startCapturingStepsForCurrentThread();
        TestNgStepRecorder.recordStep("AuthBL.signIn", TestNgCapturedStep.PASSED, 1_000_000L);
        TestNgStepRecorder.recordStep("LandingBL.startInstantMeeting", TestNgCapturedStep.FAILED, 2_000_000L);

        List<TestNgCapturedStep> steps = TestNgStepRecorder.stopCapturingAndGetStepsForCurrentThread();

        assertThat(steps).containsExactly(
                new TestNgCapturedStep("AuthBL.signIn", TestNgCapturedStep.PASSED, 1_000_000L),
                new TestNgCapturedStep("LandingBL.startInstantMeeting", TestNgCapturedStep.FAILED, 2_000_000L));
        assertThat(TestNgStepRecorder.stopCapturingAndGetStepsForCurrentThread()).isEmpty();
    }

    @Test
    void shouldDiscardStepsRecordedBeforeStartCapturingWasCalledAgain() {
        TestNgStepRecorder.startCapturingStepsForCurrentThread();
        TestNgStepRecorder.recordStep("leftover-step", TestNgCapturedStep.PASSED, 1L);

        TestNgStepRecorder.startCapturingStepsForCurrentThread();
        TestNgStepRecorder.recordStep("real-step", TestNgCapturedStep.PASSED, 1L);

        List<TestNgCapturedStep> steps = TestNgStepRecorder.stopCapturingAndGetStepsForCurrentThread();

        assertThat(steps).extracting(TestNgCapturedStep::stepName).containsExactly("real-step");
    }
}
