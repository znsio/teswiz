package com.znsio.teswiz.runner;

import com.znsio.teswiz.exceptions.InvalidTestDataException;
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
    private static final String LOG_DIR = Runner.USER_DIRECTORY + "/target/testLogs";
    private static String pdfFileName;
    private static File pdfFile;
    private static PDDocument pdfDocument;

    @BeforeAll
    public static void setupBefore() throws IOException {
        System.setProperty("LOG_DIR", LOG_DIR);
        new File(LOG_DIR).mkdirs();
        LogManager.getLogger();

        pdfFileName = "src/test/resources/pdf/Teswiz.pdf";
        pdfFile = new File(pdfFileName);
        pdfDocument = Loader.loadPDF(new RandomAccessReadBufferedFile(pdfFile.getAbsolutePath()));
    }

    @Test
    void processAllPagesByDefaultTest() throws IOException {
        int[] expectedPagesToProcess = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] pagesToProcess = Visual.getPagesToProcess(expectedPagesToProcess, pdfDocument);
        assertThat(expectedPagesToProcess).isEqualTo(pagesToProcess);
    }

    @Test
    void processAllPagesIfNullIsProvidedTest() throws IOException {
        int[] expectedPagesToProcess = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] pagesToProcess = Visual.getPagesToProcess(null, pdfDocument);
        assertThat(expectedPagesToProcess).isEqualTo(pagesToProcess);
    }

    @Test
    void processSpecificPagesByDefaultTest() throws IOException {
        int[] expectedPagesToProcess = new int[]{0, 2, 4, 6, 8, 9};
        int[] pagesToProcess = Visual.getPagesToProcess(expectedPagesToProcess, pdfDocument);
        assertThat(expectedPagesToProcess).isEqualTo(pagesToProcess);
    }

    @Test
    void outOfBoundPageNumberTest() throws IOException {
        int[] expectedPagesToProcess = new int[]{0, 19};
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

}
