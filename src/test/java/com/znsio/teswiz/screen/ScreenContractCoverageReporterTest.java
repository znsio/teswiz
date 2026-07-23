package com.znsio.teswiz.screen;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScreenContractCoverageReporterTest {
    @Test
    void shouldBuildExpectedClassNameForPlaywrightTsWeb() {
        String expectedClassName = ScreenImplementationTarget.WEB_PLAYWRIGHT_TS
                .expectedClassName("com.znsio.teswiz.screen.theapp.LoginScreen");

        assertThat(expectedClassName)
                .isEqualTo("com.znsio.teswiz.screen.web.playwright.theapp.LoginScreenPlaywrightTsAdapter");
    }

    @Test
    void shouldTreatPlaywrightTsModuleAsImplementedTarget() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString()).contains("GeneratedPlaywrightTsTestScreen");
        assertThat(report.toDisplayString())
                .doesNotContain("src/test/resources/playwright/screens/generateddemo/generated-playwright-ts-test.screen.ts");
    }

    @Test
    void shouldTreatMigratedGoogleSearchTsModulesAsImplementedTargets() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("src/test/resources/playwright/screens/googlesearch/google-search-landing.screen.ts");
        assertThat(report.toDisplayString())
                .doesNotContain("src/test/resources/playwright/screens/googlesearch/google-search-results.screen.ts");
    }

    @Test
    void shouldTreatMigratedIndigoTsModulesAsImplementedTargets() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("src/test/resources/playwright/screens/indigo/indigo-home.screen.ts");
        assertThat(report.toDisplayString())
                .doesNotContain("src/test/resources/playwright/screens/indigo/indigo-gift-vouchers.screen.ts");
        assertThat(report.toDisplayString())
                .doesNotContain("src/test/resources/playwright/screens/indigo/indigo-flight-search-results.screen.ts");
    }

    @Test
    void shouldReportMissingTargetsForContract() {
        ScreenContractCoverageReporter.CoverageReport report = new ScreenContractCoverageReporter.CoverageReport(List.of(
                new ScreenContractCoverageReporter.ContractCoverage(
                        "com.znsio.teswiz.screen.theapp.LoginScreen",
                        List.of(
                                new ScreenContractCoverageReporter.TargetCoverage("android",
                                        "com.znsio.teswiz.screen.android.theapp.LoginScreenAndroid", true),
                                new ScreenContractCoverageReporter.TargetCoverage("web-playwright-ts",
                                        "src/test/resources/playwright/screens/theapp/login.screen.ts",
                                        false)))));

        assertThat(report.toDisplayString()).contains("web-playwright-ts");
        assertThat(report.toDisplayString()).contains("with gaps");
    }

    @Test
    void shouldDiscoverCoverageFromSourceTree() throws IOException {
        Path sourceRoot = Files.createTempDirectory("screen-coverage");
        Files.createDirectories(sourceRoot.resolve("theapp"));
        Files.createDirectories(sourceRoot.resolve("android/theapp"));
        Files.createDirectories(sourceRoot.resolve("web/theapp"));

        Files.writeString(sourceRoot.resolve("theapp/AppLaunchScreen.java"), "package ignored;");
        Files.writeString(sourceRoot.resolve("android/theapp/AppLaunchScreenAndroid.java"), "package ignored;");
        Files.writeString(sourceRoot.resolve("web/theapp/AppLaunchScreenWeb.java"), "package ignored;");

        ScreenContractCoverageReporter.CoverageReport report = new ScreenContractCoverageReporter(sourceRoot)
                .buildReport();

        assertThat(report.toDisplayString()).contains("AppLaunchScreen");
        assertThat(report.toDisplayString())
                .doesNotContain("src/test/resources/playwright/screens/theapp/app-launch.screen.ts");
    }
}
