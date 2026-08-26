package com.znsio.teswiz.testng;

import com.znsio.teswiz.businessLayer.pdfValidator.PDFValidatorBL;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import org.testng.annotations.Test;

public class PDFValidationTestNgTest {
    private static final String PDF_FILE_NAME = "src/test/resources/pdf/Teswiz.pdf";

    @Test(groups = {"pdf", "standalone"})
    public void validateStandalonePdfDocument() {
        createPdfDriverForStandaloneDocument();

        new PDFValidatorBL(SAMPLE_TEST_CONTEXT.I, Runner.getPlatform())
                .validateStandalonePDFFile();
    }

    private void createPdfDriverForStandaloneDocument() {
        TestExecutionContext context = Runner.getTestExecutionContext(Thread.currentThread().getId());
        Drivers.createPDFDriverFor(SAMPLE_TEST_CONTEXT.I, Runner.getPlatform(), context, PDF_FILE_NAME);
    }
}
