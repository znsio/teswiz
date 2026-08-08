package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.exceptions.VisualTestSetupException;
import com.znsio.teswiz.tools.ScreenShotManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

class VisualTest {
    private static final Logger LOGGER = LogManager.getLogger(VisualTest.class.getName());
    private static final String WEB_CONFIG_FILE = "./configs/theapp/theapp_local_web_config.properties";
    private static String pdfFileName;
    private static File pdfFile;
    private static PDDocument pdfDocument;

    @BeforeAll
    public static void setupBefore() throws IOException {
        LOGGER.info("Running VisualTest");
        pdfFileName = "src/test/resources/pdf/Teswiz.pdf";
        pdfFile = new File(pdfFileName);
        pdfDocument = Loader.loadPDF(new RandomAccessReadBufferedFile(pdfFile.getAbsolutePath()));
    }

    @AfterEach
    void cleanUp() {
        SessionContext.remove(Thread.currentThread().getId());
        System.clearProperty(Setup.WEB_ENGINE);
    }

    @Test
    void processAllPagesByDefaultTest() throws IOException {
        int[] expectedPagesToProcess = new int[]{1, 2, 3};
        int[] pagesToProcess = Visual.getPagesToProcess(expectedPagesToProcess, pdfDocument);
        assertThat(pagesToProcess).isEqualTo(expectedPagesToProcess);
    }

    @Test
    void processAllPagesIfNullIsProvidedTest() throws IOException {
        int[] expectedPagesToProcess = new int[]{1, 2, 3};
        int[] pagesToProcess = Visual.getPagesToProcess(null, pdfDocument);
        assertThat(pagesToProcess).isEqualTo(expectedPagesToProcess);
    }

    @Test
    void processSpecificPagesByDefaultTest() throws IOException {
        int[] expectedPagesToProcess = new int[]{1, 3};
        int[] pagesToProcess = Visual.getPagesToProcess(expectedPagesToProcess, pdfDocument);
        assertThat(pagesToProcess).isEqualTo(expectedPagesToProcess);
    }

    @Test
    void allOutOfBoundPageNumberTest() throws IOException {
        int[] expectedPagesToProcess = new int[]{0, 19};
        Assertions.assertThatThrownBy(() -> Visual.getPagesToProcess(expectedPagesToProcess, pdfDocument))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("Invalid page numbers provided to process the pdf: [0, 19]");
    }

    @Test
    void partialOfBoundPageNumberTest() throws IOException {
        int[] expectedPagesToProcess = new int[]{1, 19};
        Assertions.assertThatThrownBy(() -> Visual.getPagesToProcess(expectedPagesToProcess, pdfDocument))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("Invalid page numbers provided to process the pdf: [19]");
    }

    @Test
    void onlyOutOfBoundPageNumberTest() throws IOException {
        int[] expectedPagesToProcess = new int[]{19};
        Assertions.assertThatThrownBy(() -> Visual.getPagesToProcess(expectedPagesToProcess, pdfDocument))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("Invalid page numbers provided to process the pdf: [19]");
    }

    @Test
    void borderLineOutOfBoundPageNumberTest() throws IOException {
        int[] expectedPagesToProcess = new int[]{10};
        Assertions.assertThatThrownBy(() -> Visual.getPagesToProcess(expectedPagesToProcess, pdfDocument))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("Invalid page numbers provided to process the pdf: [10]");
    }

    @Test
    void getFigmaApplitoolsConfigShouldReturnNullWhenNoValuesAreProvided() {
        TestExecutionContext context = new TestExecutionContext("no-figma-values");

        Visual.FigmaApplitoolsConfig figmaApplitoolsConfig = Visual.getFigmaApplitoolsConfig(context);

        assertThat(figmaApplitoolsConfig).isNull();
    }

    @Test
    void getFigmaApplitoolsConfigShouldReturnTrimmedValuesWhenAllValuesAreProvided() {
        TestExecutionContext context = new TestExecutionContext("valid-figma-values");
        context.addTestState(TEST_CONTEXT.APPLITOOLS_FIGMA_APP_NAME, " Applitools ");
        context.addTestState(TEST_CONTEXT.APPLITOOLS_FIGMA_TEST_NAME, " Important pages ");
        context.addTestState(TEST_CONTEXT.APPLITOOLS_FIGMA_BASELINE_ENV_NAME, " vodqa_screens ");

        Visual.FigmaApplitoolsConfig figmaApplitoolsConfig = Visual.getFigmaApplitoolsConfig(context);

        assertThat(figmaApplitoolsConfig).isNotNull();
        assertThat(figmaApplitoolsConfig.getAppName()).isEqualTo("Applitools");
        assertThat(figmaApplitoolsConfig.getTestName()).isEqualTo("Important pages");
        assertThat(figmaApplitoolsConfig.getBaselineEnvName()).isEqualTo("vodqa_screens");
    }

    @Test
    void getFigmaApplitoolsConfigShouldThrowWhenOnlySomeValuesAreProvided() {
        TestExecutionContext context = new TestExecutionContext("partial-figma-values");
        context.addTestState(TEST_CONTEXT.APPLITOOLS_FIGMA_APP_NAME, "Applitools");
        context.addTestState(TEST_CONTEXT.APPLITOOLS_FIGMA_TEST_NAME, "Important pages");

        Assertions.assertThatThrownBy(() -> Visual.getFigmaApplitoolsConfig(context))
                .isInstanceOf(VisualTestSetupException.class)
                .hasMessageContaining(TEST_CONTEXT.APPLITOOLS_FIGMA_APP_NAME)
                .hasMessageContaining(TEST_CONTEXT.APPLITOOLS_FIGMA_TEST_NAME)
                .hasMessageContaining(TEST_CONTEXT.APPLITOOLS_FIGMA_BASELINE_ENV_NAME);
    }

    @Test
    void convertValueFromConfigToIntShouldSupportMultipleNumberShapes() {
        assertThat(Visual.convertValueFromConfigToInt(720)).isEqualTo(720);
        assertThat(Visual.convertValueFromConfigToInt(720L)).isEqualTo(720);
        assertThat(Visual.convertValueFromConfigToInt(720.0d)).isEqualTo(720);
        assertThat(Visual.convertValueFromConfigToInt("720")).isEqualTo(720);
    }

    @Test
    void shouldUsePlaywrightImageEyesPathForPlaywrightWebDrivers() throws Exception {
        TestExecutionContext context = createVisualContext("playwright-visual-path");
        Setup.load(WEB_CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(WEB_CONFIG_FILE);
        Setup.getExecutionArguments();

        PlaywrightWebDriverFixture fixture = new PlaywrightWebDriverFixture();
        try {
            WebDriver playwrightDriver = fixture.createDriver("buyer");

            Visual visual = new Visual(Driver.WEB_DRIVER, Platform.web, playwrightDriver, "playwright-visual-path",
                    "buyer", "theapp");

            Object playwrightEyes = getFieldValue(visual, "eyesOnPlaywrightWeb");
            com.applitools.eyes.selenium.Eyes seleniumEyes =
                    (com.applitools.eyes.selenium.Eyes) getFieldValue(visual, "eyesOnWeb");

            assertThat(playwrightEyes).isNotNull();
            assertThat(seleniumEyes).isNotNull();
            assertThat(seleniumEyes.getIsDisabled()).isTrue();
        } finally {
            fixture.close();
        }
    }

    @Test
    void shouldKeepSeleniumEyesPathForSeleniumWebDrivers() throws Exception {
        createVisualContext("selenium-visual-path");
        Setup.load(WEB_CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(WEB_CONFIG_FILE);
        Setup.getExecutionArguments();

        WebDriver seleniumDriver = mock(WebDriver.class,
                Mockito.withSettings().extraInterfaces(JavascriptExecutor.class, TakesScreenshot.class)
                        .defaultAnswer(Answers.RETURNS_DEEP_STUBS));
        when(seleniumDriver.manage().window().getSize()).thenReturn(new Dimension(1280, 900));
        when(((JavascriptExecutor) seleniumDriver).executeScript("return (window.innerHeight);")).thenReturn(900);
        when(((JavascriptExecutor) seleniumDriver).executeScript("return (window.innerWidth);")).thenReturn(1280);
        when(((TakesScreenshot) seleniumDriver).getScreenshotAs(OutputType.BYTES)).thenReturn(new byte[]{1, 2, 3});

        Visual visual = new Visual(Driver.WEB_DRIVER, Platform.web, seleniumDriver, "selenium-visual-path",
                "buyer", "theapp");

        Object playwrightEyes = getFieldValue(visual, "eyesOnPlaywrightWeb");
        com.applitools.eyes.selenium.Eyes seleniumEyes =
                (com.applitools.eyes.selenium.Eyes) getFieldValue(visual, "eyesOnWeb");

        assertThat(playwrightEyes).isNull();
        assertThat(seleniumEyes).isNotNull();
    }

    private static TestExecutionContext createVisualContext(String testName) throws IOException {
        TestExecutionContext context = new TestExecutionContext(testName);
        Path scenarioDirectory = Files.createTempDirectory(testName + "-scenario");
        Path screenshotDirectory = Files.createDirectories(scenarioDirectory.resolve("screenshots"));
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, scenarioDirectory.toString());
        context.addTestState(TEST_CONTEXT.SCREENSHOT_DIRECTORY, screenshotDirectory.toString());
        context.addTestState(TEST_CONTEXT.SCREENSHOT_MANAGER, new ScreenShotManager());
        return context;
    }

    private static Object getFieldValue(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

}
