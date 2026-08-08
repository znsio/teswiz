# PDF Validation Test Implementation Example

This guide provides a concrete example of implementing a PDF validation test case in Teswiz. PDF validation relies on Applitools Visual AI to verify layout, text, and visual elements on specific PDF pages.

---

## 1. Feature File (`pdf-validation.feature`)
```gherkin
@pdf
Feature: PDF Invoice Validation

  Scenario: Verify billing details layout in generated invoice PDF
    Given I load the PDF file "invoice_2026.pdf"
    Then I validate pages 1 and 2 of the PDF visually
```

---

## 2. Step Definition (`PDFSteps.java`)
```java
package com.znsio.sample.e2e.steps;

import com.znsio.teswiz.businessLayer.pdfValidator.PDFValidatorBL;
import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class PDFSteps {
    private final TestExecutionContext context;

    public PDFSteps() {
        long threadId = Thread.currentThread().getId();
        this.context = SessionContext.getTestExecutionContext(threadId);
    }

    @Given("I load the PDF file {string}")
    public void loadPdfFile(String pdfFileName) {
        String persona = "me";
        context.addTestState(TEST_CONTEXT.PDF_FILE_NAME, pdfFileName);
        
        // Create the specialized PDF Driver using Drivers wrapper
        Drivers.createPDFDriverFor(persona, Platform.pdf, context, pdfFileName);
    }

    @Then("I validate pages {int} and {int} of the PDF visually")
    public void validatePdfPages(int page1, int page2) {
        int[] pagesToValidate = new int[]{page1, page2};
        new PDFValidatorBL("me", Platform.pdf)
                .validateStandalonePDFFile(pagesToValidate);
    }
}
```

---

## 3. Business Layer (`PDFValidatorBL.java`)
```java
package com.znsio.teswiz.businessLayer.pdfValidator;

import com.applitools.eyes.TestResults;
import com.applitools.eyes.TestResultsStatus;
import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import org.assertj.core.api.SoftAssertions;

public class PDFValidatorBL {
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final Visual visually;

    public PDFValidatorBL(String userPersona, Platform platform) {
        long threadId = Thread.currentThread().getId();
        this.context = SessionContext.getTestExecutionContext(threadId);
        this.softly = Runner.getSoftAssertion(threadId);
        this.visually = Runner.getVisual(threadId);
        Runner.setCurrentDriverForUser(userPersona, platform, context);
    }

    public PDFValidatorBL validateStandalonePDFFile(int[] pageRange) {
        String pdfFileName = context.getTestStateAsString(TEST_CONTEXT.PDF_FILE_NAME);
        
        // Validate specific pages of the loaded PDF using Applitools Visual AI
        TestResults testResults = visually.validatePdf(pageRange);
        
        softly.assertThat(testResults.getStatus())
                .as("Visual verification failed for PDF: %s", pdfFileName)
                .isEqualTo(TestResultsStatus.Passed);
                
        return this;
    }
}
```
