package com.znsio.teswiz.testng;

import com.znsio.teswiz.businessLayer.pdfValidator.PDFValidatorBL;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import org.testng.annotations.Test;

public class PDFValidationTestNgTest {
    @Test(groups = {"pdf", "standalone"})
    public void validateStandalonePdfDocument() {
        TestExecutionContext context = Runner.getTestExecutionContext(Thread.currentThread().getId());
        String pdfFileName = "src/test/resources/pdf/Teswiz.pdf";
        Drivers.createPDFDriverFor(SAMPLE_TEST_CONTEXT.I, Runner.getPlatform(), context, pdfFileName);
        new PDFValidatorBL(SAMPLE_TEST_CONTEXT.I, Runner.getPlatform()).validateStandalonePDFFile();
    }
}
