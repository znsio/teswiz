package com.znsio.teswiz.aspect;

import com.znsio.teswiz.businessLayer.aspectfixture.AspectFixtureBL;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.testng.TestNgCapturedStep;
import com.znsio.teswiz.testng.TestNgStepRecorder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestNgStepCaptureAspectTest {
    private static final String cliConfigFilePath = "./configs/cli_local_config.properties";

    @AfterEach
    void clearFrameworkOverride() {
        System.clearProperty(Setup.FRAMEWORK);
        TestNgStepRecorder.stopCapturingAndGetStepsForCurrentThread();
    }

    @Test
    void shouldCaptureAPassingConsumerLayerCallWhenInTestNgMode() {
        System.setProperty(Setup.FRAMEWORK, "testng");
        Setup.load(cliConfigFilePath);
        Setup.getExecutionArguments();
        TestNgStepRecorder.startCapturingStepsForCurrentThread();

        new AspectFixtureBL().doSomething();

        List<TestNgCapturedStep> steps = TestNgStepRecorder.stopCapturingAndGetStepsForCurrentThread();
        assertThat(steps).anySatisfy(step -> {
            assertThat(step.stepName()).contains("AspectFixtureBL").contains("doSomething");
            assertThat(step.status()).isEqualTo(TestNgCapturedStep.PASSED);
        });
    }

    @Test
    void shouldCaptureAFailingConsumerLayerCallWhenInTestNgMode() {
        System.setProperty(Setup.FRAMEWORK, "testng");
        Setup.load(cliConfigFilePath);
        Setup.getExecutionArguments();
        TestNgStepRecorder.startCapturingStepsForCurrentThread();

        assertThatThrownBy(() -> new AspectFixtureBL().doSomethingThatFails())
                .isInstanceOf(InvalidTestDataException.class);

        List<TestNgCapturedStep> steps = TestNgStepRecorder.stopCapturingAndGetStepsForCurrentThread();
        assertThat(steps).anySatisfy(step -> {
            assertThat(step.stepName()).contains("doSomethingThatFails");
            assertThat(step.status()).isEqualTo(TestNgCapturedStep.FAILED);
        });
    }

    @Test
    void shouldNotCaptureAnythingWhenInCucumberMode() {
        System.setProperty(Setup.FRAMEWORK, "cucumber");
        Setup.load(cliConfigFilePath);
        Setup.getExecutionArguments();
        TestNgStepRecorder.startCapturingStepsForCurrentThread();

        new AspectFixtureBL().doSomething();

        assertThat(TestNgStepRecorder.stopCapturingAndGetStepsForCurrentThread()).isEmpty();
    }
}
