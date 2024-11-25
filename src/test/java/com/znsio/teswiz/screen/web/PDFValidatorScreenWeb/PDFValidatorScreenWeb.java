package com.znsio.teswiz.screen.web.PDFValidatorScreenWeb;

import com.applitools.eyes.TestResults;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.pdfValidator.PDFValidatorScreen;
import org.apache.commons.lang3.NotImplementedException;

public class PDFValidatorScreenWeb extends PDFValidatorScreen {
    private static final String NOT_YET_IMPLEMENTED = "Not yet implemented";
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = PDFValidatorScreenWeb.class.getSimpleName();

    public PDFValidatorScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public TestResults validatePDF() {
        throw new NotImplementedException(
                SCREEN_NAME + ":" + new Throwable().getStackTrace()[0].getMethodName() + NOT_YET_IMPLEMENTED);
    }

    @Override
    public TestResults validatePDF(int[] pageNumbers) {
        throw new NotImplementedException(
                SCREEN_NAME + ":" + new Throwable().getStackTrace()[0].getMethodName() + NOT_YET_IMPLEMENTED);
    }

    @Override
    public TestResults validatePDF(String pdfFileName) {
        return visually.validatePdf(pdfFileName);
    }

    @Override
    public TestResults validatePDF(String pdfFileName, int[] pageNumbers) {
        return visually.validatePdf(pdfFileName, pageNumbers);
    }
}
