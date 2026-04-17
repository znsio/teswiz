package com.znsio.teswiz.runner;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.exceptions.VisualTestSetupException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class VisualTest {
    private static final Logger LOGGER = LogManager.getLogger(VisualTest.class.getName());
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

}
