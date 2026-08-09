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

        assertThat(violations).contains("missing public constructor (Driver, Visual) or (PlaywrightJavaScreenContext)");
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
                        List.of(
                                "missing Java screen implementations",
                                "missing playwright-ts module: src/test/resources/playwright/screens/theapp/app-launch.screen.ts"))));

        assertThat(report.toDisplayString()).contains("missing Java screen implementations");
        assertThat(report.toDisplayString())
                .contains("missing playwright-ts module: src/test/resources/playwright/screens/theapp/app-launch.screen.ts");
    }

    @Test
    void shouldGroupJavaAndPlaywrightTsIssuesSeparatelyInReport() {
        ScreenContractSanityChecker.ValidationReport report = new ScreenContractSanityChecker.ValidationReport(List.of(
                new ScreenContractSanityChecker.ContractValidationResult(
                        "com.znsio.teswiz.screen.theapp.LoginScreen",
                        List.of("com.znsio.teswiz.screen.web.theapp.LoginScreenWeb"),
                        List.of(
                                "missing public constructor (Driver, Visual) or (PlaywrightJavaScreenContext)",
                                "missing playwright-ts module: src/test/resources/playwright/screens/theapp/login.screen.ts",
                                "src/test/resources/playwright/screens/theapp/login.screen.ts: missing exported function: dismissAlert()"))));

        assertThat(report.toDisplayString())
                .contains("Java implementations: com.znsio.teswiz.screen.web.theapp.LoginScreenWeb")
                .contains("Java issues:")
                .contains("missing public constructor (Driver, Visual) or (PlaywrightJavaScreenContext)")
                .contains("Playwright-TS module issues:")
                .contains("missing playwright-ts module: src/test/resources/playwright/screens/theapp/login.screen.ts")
                .contains("src/test/resources/playwright/screens/theapp/login.screen.ts: missing exported function: dismissAlert()");
    }

    @Test
    void shouldValidatePlaywrightTsModuleExportsAgainstContract() {
        ScreenContractSanityChecker checker =
                new ScreenContractSanityChecker(java.nio.file.Path.of("src/test/java/com/znsio/teswiz/screen"));

        ScreenContractSanityChecker.ContractValidationResult result =
                checker.validateContract("com.znsio.teswiz.screen.generateddemo.GeneratedPlaywrightTsTestScreen");

        assertThat(result.violations()).isEmpty();
    }

    @Test
    void shouldValidateMigratedGoogleSearchTsModulesAgainstContracts() {
        ScreenContractSanityChecker checker =
                new ScreenContractSanityChecker(java.nio.file.Path.of("src/test/java/com/znsio/teswiz/screen"));

        ScreenContractSanityChecker.ContractValidationResult landingResult =
                checker.validateContract("com.znsio.teswiz.screen.googlesearch.GoogleSearchLandingScreen");
        ScreenContractSanityChecker.ContractValidationResult resultsResult =
                checker.validateContract("com.znsio.teswiz.screen.googlesearch.GoogleSearchResultsScreen");

        assertThat(landingResult.violations()).isEmpty();
        assertThat(resultsResult.violations()).isEmpty();
    }

    @Test
    void shouldValidateMigratedIndigoTsModulesAgainstContracts() {
        ScreenContractSanityChecker checker =
                new ScreenContractSanityChecker(java.nio.file.Path.of("src/test/java/com/znsio/teswiz/screen"));

        ScreenContractSanityChecker.ContractValidationResult homeResult =
                checker.validateContract("com.znsio.teswiz.screen.indigo.IndigoHomeScreen");
        ScreenContractSanityChecker.ContractValidationResult voucherResult =
                checker.validateContract("com.znsio.teswiz.screen.indigo.IndigoGiftVouchersScreen");
        ScreenContractSanityChecker.ContractValidationResult resultsResult =
                checker.validateContract("com.znsio.teswiz.screen.indigo.IndigoFlightSearchResultsScreen");

        assertThat(homeResult.violations()).isEmpty();
        assertThat(voucherResult.violations()).isEmpty();
        assertThat(resultsResult.violations()).isEmpty();
    }

    @Test
    void shouldValidateMigratedJioMeetTsModulesAgainstContracts() {
        ScreenContractSanityChecker checker =
                new ScreenContractSanityChecker(java.nio.file.Path.of("src/test/java/com/znsio/teswiz/screen"));

        ScreenContractSanityChecker.ContractValidationResult landingResult =
                checker.validateContract("com.znsio.teswiz.screen.jiomeet.LandingScreen");
        ScreenContractSanityChecker.ContractValidationResult signInResult =
                checker.validateContract("com.znsio.teswiz.screen.jiomeet.SignInScreen");
        ScreenContractSanityChecker.ContractValidationResult inAMeetingResult =
                checker.validateContract("com.znsio.teswiz.screen.jiomeet.InAMeetingScreen");

        assertThat(landingResult.violations()).isEmpty();
        assertThat(signInResult.violations()).isEmpty();
        assertThat(inAMeetingResult.violations()).isEmpty();
    }

    @Test
    void shouldValidateMigratedScreenshotTsModuleAgainstContract() {
        ScreenContractSanityChecker checker =
                new ScreenContractSanityChecker(java.nio.file.Path.of("src/test/java/com/znsio/teswiz/screen"));

        ScreenContractSanityChecker.ContractValidationResult result =
                checker.validateContract("com.znsio.teswiz.screen.ScreenShotScreen");

        assertThat(result.violations()).isEmpty();
    }

    @Test
    void shouldNotReportMissingTargetCoverageByDefault() {
        ScreenContractSanityChecker checker =
                new ScreenContractSanityChecker(java.nio.file.Path.of("src/test/java/com/znsio/teswiz/screen"));

        ScreenContractSanityChecker.ContractValidationResult result =
                checker.validateContract("com.znsio.teswiz.screen.theapp.LoginScreen");

        assertThat(result.violations())
                .noneMatch(violation -> violation.startsWith("missing target coverage:"));
    }

    @Test
    void shouldOptionallyReportMissingTargetCoverage() {
        ScreenContractSanityChecker checker =
                new ScreenContractSanityChecker(java.nio.file.Path.of("src/test/java/com/znsio/teswiz/screen"),
                        true);

        ScreenContractSanityChecker.ContractValidationResult result =
                checker.validateContract("com.znsio.teswiz.screen.theapp.LoginScreen");

        assertThat(result.violations())
                .anyMatch(violation -> violation.startsWith("missing target coverage: windows"))
                .anyMatch(violation -> violation.startsWith("missing target coverage: pdf"));
    }

    @Test
    void shouldGroupTargetCoverageIssuesSeparatelyInReport() {
        ScreenContractSanityChecker.ValidationReport report = new ScreenContractSanityChecker.ValidationReport(List.of(
                new ScreenContractSanityChecker.ContractValidationResult(
                        "com.znsio.teswiz.screen.theapp.LoginScreen",
                        List.of("com.znsio.teswiz.screen.web.theapp.LoginScreenWeb"),
                        List.of(
                                "missing public constructor (Driver, Visual) or (PlaywrightJavaScreenContext)",
                                "missing target coverage: android -> com.znsio.teswiz.screen.android.theapp.LoginScreenAndroid",
                                "missing playwright-ts module: src/test/resources/playwright/screens/theapp/login.screen.ts"))));

        assertThat(report.toDisplayString())
                .contains("Java issues:")
                .contains("Playwright-TS module issues:")
                .contains("Target coverage issues:")
                .contains("missing target coverage: android -> com.znsio.teswiz.screen.android.theapp.LoginScreenAndroid");
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
