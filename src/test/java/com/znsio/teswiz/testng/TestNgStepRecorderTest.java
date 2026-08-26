package com.znsio.teswiz.testng;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestNgStepRecorderTest {

    @Test
    void shouldReturnCompletedStepsInStartOrderAndClearAfterwards() {
        TestNgStepRecorder.startCapturingStepsForCurrentThread();

        int firstIndex = TestNgStepRecorder.beginStep("AuthBL.signIn", "com.znsio.teswiz.businessLayer.AuthBL.signIn(String)");
        TestNgStepRecorder.endStep(firstIndex, TestNgCapturedStep.PASSED, 1_000_000L);

        int secondIndex = TestNgStepRecorder.beginStep("LandingBL.startInstantMeeting", "com.znsio.teswiz.businessLayer.LandingBL.startInstantMeeting()");
        TestNgStepRecorder.endStep(secondIndex, TestNgCapturedStep.FAILED, 2_000_000L);

        List<TestNgCapturedStep> steps = TestNgStepRecorder.stopCapturingAndGetStepsForCurrentThread();

        assertThat(steps).containsExactly(
                new TestNgCapturedStep("AuthBL.signIn", "com.znsio.teswiz.businessLayer.AuthBL.signIn(String)",
                        0, TestNgCapturedStep.PASSED, 1_000_000L),
                new TestNgCapturedStep("LandingBL.startInstantMeeting", "com.znsio.teswiz.businessLayer.LandingBL.startInstantMeeting()",
                        0, TestNgCapturedStep.FAILED, 2_000_000L));
        assertThat(TestNgStepRecorder.stopCapturingAndGetStepsForCurrentThread()).isEmpty();
    }

    @Test
    void shouldTrackNestingDepthAndPreserveStartOrderForCallsMadeWhileAnOuterStepIsInProgress() {
        TestNgStepRecorder.startCapturingStepsForCurrentThread();

        int outerIndex = TestNgStepRecorder.beginStep("AppBL.provideInvalidDetailsForSignup", "location.outer()");
        int innerIndex = TestNgStepRecorder.beginStep("LoginScreenWeb.enterLoginDetails", "location.inner()");
        TestNgStepRecorder.endStep(innerIndex, TestNgCapturedStep.PASSED, 1L);
        TestNgStepRecorder.endStep(outerIndex, TestNgCapturedStep.PASSED, 2L);

        List<TestNgCapturedStep> steps = TestNgStepRecorder.stopCapturingAndGetStepsForCurrentThread();

        assertThat(steps).extracting(TestNgCapturedStep::stepName)
                .containsExactly("AppBL.provideInvalidDetailsForSignup", "LoginScreenWeb.enterLoginDetails");
        assertThat(steps.get(0).depth()).isEqualTo(0);
        assertThat(steps.get(1).depth()).isEqualTo(1);
    }

    @Test
    void shouldResetNestingDepthAfterASiblingStepCompletes() {
        TestNgStepRecorder.startCapturingStepsForCurrentThread();

        int firstOuterIndex = TestNgStepRecorder.beginStep("outer-1", "outer1()");
        int firstInnerIndex = TestNgStepRecorder.beginStep("inner-1", "inner1()");
        TestNgStepRecorder.endStep(firstInnerIndex, TestNgCapturedStep.PASSED, 1L);
        TestNgStepRecorder.endStep(firstOuterIndex, TestNgCapturedStep.PASSED, 1L);

        int secondOuterIndex = TestNgStepRecorder.beginStep("outer-2", "outer2()");
        TestNgStepRecorder.endStep(secondOuterIndex, TestNgCapturedStep.PASSED, 1L);

        List<TestNgCapturedStep> steps = TestNgStepRecorder.stopCapturingAndGetStepsForCurrentThread();

        assertThat(steps.get(2).stepName()).isEqualTo("outer-2");
        assertThat(steps.get(2).depth()).as("depth should return to 0 once the first outer step completed").isEqualTo(0);
    }

    @Test
    void shouldDiscardStepsRecordedBeforeStartCapturingWasCalledAgain() {
        TestNgStepRecorder.startCapturingStepsForCurrentThread();
        int leftoverIndex = TestNgStepRecorder.beginStep("leftover-step", "leftover.Location()");
        TestNgStepRecorder.endStep(leftoverIndex, TestNgCapturedStep.PASSED, 1L);

        TestNgStepRecorder.startCapturingStepsForCurrentThread();
        int realIndex = TestNgStepRecorder.beginStep("real-step", "real.Location()");
        TestNgStepRecorder.endStep(realIndex, TestNgCapturedStep.PASSED, 1L);

        List<TestNgCapturedStep> steps = TestNgStepRecorder.stopCapturingAndGetStepsForCurrentThread();

        assertThat(steps).extracting(TestNgCapturedStep::stepName).containsExactly("real-step");
    }
}
