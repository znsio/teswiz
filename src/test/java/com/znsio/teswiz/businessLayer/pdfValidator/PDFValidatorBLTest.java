package com.znsio.teswiz.businessLayer.pdfValidator;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.applitools.eyes.TestResults;
import com.applitools.eyes.TestResultsStatus;
import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.session.UserPersonaDetails;

class PDFValidatorBLTest {
    private static final String CONFIG_FILE = "./configs/theapp/theapp_lambdatest_web_config.properties";

    @AfterEach
    void cleanUp() {
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void validateShouldUseCurrentScenarioDriverEvenWhenConstructorUsesMeAndPdf() {
        setupConfig();
        TestExecutionContext context = new TestExecutionContext("pdf-validator-web-scenario");
        UserPersonaDetails userPersonaDetails = new UserPersonaDetails();
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS, userPersonaDetails);
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA, TEST_CONTEXT.I);
        context.addTestState(TEST_CONTEXT.CURRENT_PLATFORM, Platform.web);
        context.addTestState(TEST_CONTEXT.SOFT_ASSERTIONS, new SoftAssertions());

        Driver currentDriver = mock(Driver.class);
        Visual currentVisual = mock(Visual.class);
        TestResults testResults = mock(TestResults.class);
        when(currentDriver.getVisual()).thenReturn(currentVisual);
        when(currentVisual.validatePdf("src/test/resources/pdf/Teswiz.pdf")).thenReturn(testResults);
        when(testResults.getStatus()).thenReturn(TestResultsStatus.Passed);
        userPersonaDetails.addDriver(TEST_CONTEXT.I, currentDriver);
        userPersonaDetails.addPlatform(TEST_CONTEXT.I, Platform.web);

        new PDFValidatorBL("I", Platform.pdf).validate("src/test/resources/pdf/Teswiz.pdf");
    }

    private static void setupConfig() {
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
    }
}
