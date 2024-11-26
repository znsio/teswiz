package com.znsio.teswiz.screen.pdf;

import com.applitools.eyes.TestResults;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.pdfValidator.PDFValidatorScreen;
import org.apache.commons.lang3.NotImplementedException;

public class PDFValidatorScreenPDF extends PDFValidatorScreen {
    private static final String NOT_YET_IMPLEMENTED = "Not yet implemented";
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = PDFValidatorScreenPDF.class.getSimpleName();

    public PDFValidatorScreenPDF(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public TestResults validatePDF() {
        return visually.validatePdf();
    }

    @Override
    public TestResults validatePDF(int[] pageNumbers) {
        return visually.validatePdf(pageNumbers);
    }

    @Override
    public TestResults validatePDF(String pdfFileName) {
        throw new NotImplementedException(
                SCREEN_NAME + ":" + new Throwable().getStackTrace()[0].getMethodName() + NOT_YET_IMPLEMENTED);
    }

    @Override
    public TestResults validatePDF(String pdfFileName, int[] pageNumbers) {
        throw new NotImplementedException(
                SCREEN_NAME + ":" + new Throwable().getStackTrace()[0].getMethodName() + NOT_YET_IMPLEMENTED);
    }

}
