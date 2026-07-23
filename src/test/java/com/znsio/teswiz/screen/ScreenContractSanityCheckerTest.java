package com.znsio.teswiz.screen;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScreenContractSanityCheckerTest {
    @Test
    void shouldAcceptCompliantImplementation() {
        List<String> violations = ScreenContractSanityChecker.validateImplementation(
                ExampleContract.class, ExampleImplementation.class);

        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFlagMissingDriverVisualConstructor() {
        List<String> violations = ScreenContractSanityChecker.validateImplementation(
                ExampleContract.class, MissingConstructorImplementation.class);

        assertThat(violations).contains("missing public constructor (Driver, Visual)");
    }

    @Test
    void shouldFlagImplementationThatDoesNotImplementContract() {
        List<String> violations = ScreenContractSanityChecker.validateImplementation(
                ExampleContract.class, NotAContractImplementation.class);

        assertThat(violations).contains("does not implement " + ExampleContract.class.getName());
    }

    @Test
    void shouldReportMissingImplementationsForContract() {
        ScreenContractSanityChecker.ValidationReport report = new ScreenContractSanityChecker.ValidationReport(List.of(
                new ScreenContractSanityChecker.ContractValidationResult(
                        "com.znsio.teswiz.screen.theapp.AppLaunchScreen",
                        List.of(),
                        List.of("No screen implementations found"))));

        assertThat(report.toDisplayString()).contains("No screen implementations found");
    }

    @Test
    void shouldValidatePlaywrightTsModuleExportsAgainstContract() {
        ScreenContractSanityChecker checker =
                new ScreenContractSanityChecker(java.nio.file.Path.of("src/test/java/com/znsio/teswiz/screen"));

        ScreenContractSanityChecker.ContractValidationResult result =
                checker.validateContract("com.znsio.teswiz.screen.generateddemo.GeneratedPlaywrightTsTestScreen");

        assertThat(result.violations()).isEmpty();
    }

    public abstract static class ExampleContract {
        public abstract ExampleContract doSomething(String value);
    }

    public static class ExampleImplementation extends ExampleContract {
        public ExampleImplementation(Driver driver, Visual visually) {
        }

        @Override
        public ExampleContract doSomething(String value) {
            return this;
        }
    }

    public static class MissingConstructorImplementation extends ExampleContract {
        public MissingConstructorImplementation() {
        }

        @Override
        public ExampleContract doSomething(String value) {
            return this;
        }
    }

    public static class NotAContractImplementation {
        public NotAContractImplementation(Driver driver, Visual visually) {
        }
    }
}
