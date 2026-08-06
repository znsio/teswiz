package com.znsio.teswiz.screen;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScreenContractCoverageReporterTest {
    @Test
    void shouldBuildExpectedModulePathForPlaywrightTsWeb() {
        String expectedModulePath = ScreenImplementationTarget.WEB_PLAYWRIGHT_TS
                .expectedReference("com.znsio.teswiz.screen.theapp.LoginScreen");

        assertThat(expectedModulePath)
                .isEqualTo("src/test/resources/playwright/screens/theapp/login.screen.ts");
    }

    @Test
    void shouldBuildExpectedClassNameForPlaywrightJavaWeb() {
        String expectedClassName = ScreenImplementationTarget.WEB_PLAYWRIGHT_JAVA
                .expectedClassName("com.znsio.teswiz.screen.theapp.LoginScreen");

        assertThat(expectedClassName)
                .isEqualTo("com.znsio.teswiz.screen.web.playwrightjava.theapp.LoginScreenPlaywrightJava");
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
    void shouldTreatGoogleSearchPlaywrightJavaScreensAsImplementedTargets() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.googlesearch.GoogleSearchLandingScreenPlaywrightJava");
        assertThat(report.toDisplayString())
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.googlesearch.GoogleSearchResultsScreenPlaywrightJava");
    }

    @Test
    void shouldTreatTheAppFileUploadPlaywrightJavaScreenAsImplementedTarget() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.theapp.FileUploadScreenPlaywrightJava");
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
    void shouldTreatIndigoPlaywrightJavaScreensAsImplementedTargets() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.indigo.IndigoHomeScreenPlaywrightJava");
        assertThat(report.toDisplayString())
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.indigo.IndigoGiftVouchersScreenPlaywrightJava");
        assertThat(report.toDisplayString())
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.indigo.IndigoFlightSearchResultsScreenPlaywrightJava");
    }

    @Test
    void shouldTreatMigratedJioMeetTsModulesAsImplementedTargets() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("src/test/resources/playwright/screens/jiomeet/landing.screen.ts");
        assertThat(report.toDisplayString())
                .doesNotContain("src/test/resources/playwright/screens/jiomeet/sign-in.screen.ts");
        assertThat(report.toDisplayString())
                .doesNotContain("src/test/resources/playwright/screens/jiomeet/in-a-meeting.screen.ts");
    }

    @Test
    void shouldTreatDineoutTsModuleAsImplementedTarget() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("src/test/resources/playwright/screens/dineout/dineout-landing.screen.ts");
    }

    @Test
    void shouldTreatConfEngineTsModuleAsImplementedTarget() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("src/test/resources/playwright/screens/confengine/conf-engine-landing.screen.ts");
    }

    @Test
    void shouldTreatUnsupportedPlaywrightTsModulesAsImplementedTargets() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("src/test/resources/playwright/screens/helloWorld/hello-world.screen.ts")
                .doesNotContain("src/test/resources/playwright/screens/notepad/notepad.screen.ts")
                .doesNotContain("src/test/resources/playwright/screens/duckduckgo/duck-duck-go.screen.ts")
                .doesNotContain("src/test/resources/playwright/screens/autoscroll/auto-scroll.screen.ts")
                .doesNotContain("src/test/resources/playwright/screens/calculator/calculator.screen.ts")
                .doesNotContain("src/test/resources/playwright/screens/calculator/new-calculator.screen.ts")
                .doesNotContain("src/test/resources/playwright/screens/jiocinema/jio-cinema.screen.ts")
                .doesNotContain("src/test/resources/playwright/screens/vodqa/vodqa.screen.ts")
                .doesNotContain("src/test/resources/playwright/screens/vodqa/drag-and-drop.screen.ts")
                .doesNotContain("src/test/resources/playwright/screens/vodqa/native-view.screen.ts")
                .doesNotContain("src/test/resources/playwright/screens/vodqa/web-view.screen.ts");
    }

    @Test
    void shouldTreatJioMeetPlaywrightJavaScreensAsImplementedTargets() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.jiomeet.LandingScreenPlaywrightJava");
        assertThat(report.toDisplayString())
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.jiomeet.SignInScreenPlaywrightJava");
        assertThat(report.toDisplayString())
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.jiomeet.InAMeetingScreenPlaywrightJava");
    }

    @Test
    void shouldTreatMigratedScreenshotTsModuleAsImplementedTarget() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("src/test/resources/playwright/screens/screen-shot.screen.ts");
    }

    @Test
    void shouldTreatScreenshotPlaywrightJavaScreenAsImplementedTarget() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.ScreenShotScreenPlaywrightJava");
    }

    @Test
    void shouldTreatDineoutPlaywrightJavaScreenAsImplementedTarget() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.dineout.DineoutLandingScreenPlaywrightJava");
    }

    @Test
    void shouldTreatGeneratedDemoPlaywrightJavaScreenAsImplementedTarget() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.generateddemo.GeneratedPlaywrightTsTestScreenPlaywrightJava");
    }

    @Test
    void shouldTreatConfEnginePlaywrightJavaScreenAsImplementedTarget() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.confengine.ConfEngineLandingScreenPlaywrightJava");
    }

    @Test
    void shouldTreatVodqaPlaywrightJavaScreensAsImplementedTargets() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.vodqa.VodqaScreenPlaywrightJava")
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.vodqa.DragAndDropScreenPlaywrightJava")
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.vodqa.NativeViewScreenPlaywrightJava")
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.vodqa.WebViewScreenPlaywrightJava");
    }

    @Test
    void shouldTreatUnsupportedPlaywrightJavaScreensAsImplementedTargets() throws IOException {
        ScreenContractCoverageReporter.CoverageReport report =
                new ScreenContractCoverageReporter(Path.of("src/test/java/com/znsio/teswiz/screen")).buildReport();

        assertThat(report.toDisplayString())
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.helloWorld.HelloWorldScreenPlaywrightJava")
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.notepad.NotepadScreenPlaywrightJava")
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.duckduckgo.DuckDuckGoScreenPlaywrightJava")
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.autoscroll.AutoScrollScreenPlaywrightJava")
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.calculator.CalculatorScreenPlaywrightJava")
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.calculator.NewCalculatorScreenPlaywrightJava")
                .doesNotContain("com.znsio.teswiz.screen.web.playwrightjava.jiocinema.JioCinemaScreenPlaywrightJava");
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
