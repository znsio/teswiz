package com.znsio.teswiz.runner;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.assertj.core.api.Assertions.assertThat;

class DriversBasicTest {
    private static final String CONFIG_FILE = "./configs/theapp/theapp_lambdatest_web_config.properties";

    @AfterEach
    void cleanUp() {
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void getCapabilityForShouldReturnValueOrEmptyString() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("browserName", "chrome");

        assertThat(Drivers.getCapabilityFor(capabilities, "browserName")).isEqualTo("chrome");
        assertThat(Drivers.getCapabilityFor(capabilities, "unknownCapability")).isEmpty();
    }

    @Test
    void createPdfDriverForShouldRegisterUserPersonaAndMakeItAvailable() {
        setupConfig();
        TestExecutionContext context = new TestExecutionContext("drivers-pdf-test");
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS, new UserPersonaDetails());

        Drivers.createPDFDriverFor("me", Platform.pdf, context, "sample.pdf");

        assertThat(Drivers.isDriverAssignedForUser("me")).isTrue();
        assertThat(Drivers.getAvailableUserPersonas()).contains("me");
        assertThat(Drivers.getDriverForUser("me").getType()).isEqualTo(Driver.PDF_DRIVER);
    }

    private static void setupConfig() {
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
    }
}
