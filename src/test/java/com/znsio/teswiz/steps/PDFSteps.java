package com.znsio.teswiz.steps;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.businessLayer.pdfValidator.PDFValidatorBL;
import com.znsio.teswiz.businessLayer.theapp.AppBL;
import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class PDFSteps {
    private static final Logger LOGGER = LogManager.getLogger(PDFSteps.class.getName());
    private final TestExecutionContext context;

    public PDFSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @Given("I can validate the pdf document {string}")
    public void iCanValidateThePdfDocument(String pdfFileName) {
        LOGGER.info("Validate the pdf document " + pdfFileName);
        new PDFValidatorBL(SAMPLE_TEST_CONTEXT.I, Runner.getPlatform()).validate(pdfFileName);
    }

    @Given("{string} go to Login")
    public void goToLogin(String userPersona) {
        LOGGER.info("Launch the app: " + userPersona);
        Platform currentPlatform = Runner.getPlatform();
        Drivers.createDriverFor(userPersona, currentPlatform, context);
        new AppBL(userPersona, currentPlatform).goToLogin();
    }

    @Then("I validate all pages of the pdf document {string}")
    public void iValidateAllPagesOfThePdfDocument(String pdfFileName) {
        LOGGER.info("Validate all pages of the pdf document %s".formatted(pdfFileName));
        new PDFValidatorBL(SAMPLE_TEST_CONTEXT.I, Platform.pdf).validate(pdfFileName);
    }

    @And("I validate page numbers {string} of the pdf document {string}")
    public void iValidatePageNumbersOfThePdfDocument(String pageNumbers, String pdfFileName) {
        int[] intArray = Arrays.stream(pageNumbers.trim().split(","))
                .mapToInt(Integer::parseInt)
                .toArray();
        LOGGER.info(
                "Validate page numbers: '%s' of the pdf document %s".formatted(Arrays.toString(intArray), pdfFileName));
        new PDFValidatorBL(SAMPLE_TEST_CONTEXT.I, Platform.pdf).validate(pdfFileName, intArray);
    }

    @Given("I validate the standalone pdf document {string}")
    public void iValidateTheStandalonePdfDocument(String pdfFileName) {
        LOGGER.info("Validate all pages of the standalone pdf: %s".formatted(pdfFileName));
        Drivers.createPDFDriverFor(SAMPLE_TEST_CONTEXT.I, Runner.getPlatform(), context, pdfFileName);
        new PDFValidatorBL(SAMPLE_TEST_CONTEXT.I, Runner.getPlatform()).validateStandalonePDFFile();
    }

    @And("I validate page numbers {string} of standalone pdf document {string}")
    public void iValidatePageNumbersOfStandalonePdfDocument(String pageNumbers, String pdfFileName) {
        int[] intArray = Arrays.stream(pageNumbers.trim().split(","))
                .mapToInt(Integer::parseInt)
                .toArray();
        LOGGER.info("Validate page numbers: '%s' of standalone pdf document %s".formatted(Arrays.toString(intArray),
                pdfFileName));
        Drivers.createPDFDriverFor(SAMPLE_TEST_CONTEXT.I, Runner.getPlatform(), context, pdfFileName);
        new PDFValidatorBL(SAMPLE_TEST_CONTEXT.I, Runner.getPlatform()).validateStandalonePDFFile(intArray);
    }
}
