package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.session.SessionHandle;
import com.znsio.teswiz.session.UserPersonaDetails;
import com.znsio.teswiz.tools.ScreenShotManager;
import com.znsio.teswiz.web.WebEngine;
import com.znsio.teswiz.web.playwright.PlaywrightWebDriver;

class DriversPlaywrightIntegrationTest {
    private static final String CONFIG_FILE = "./configs/theapp/theapp_local_web_config.properties";

    @AfterEach
    void cleanUp() {
        System.clearProperty("WEB_ENGINE");
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void shouldCreatePlaywrightBackedDriverAndSessionHandle() throws Exception {
        System.setProperty("WEB_ENGINE", "playwright-ts");
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);

        TestExecutionContext context = new TestExecutionContext("playwright-driver-integration");
        Path scenarioDir = Files.createTempDirectory("playwright-driver-integration");
        Path screenshotsDir = Files.createDirectory(scenarioDir.resolve("screenshots"));
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, scenarioDir.toString());
        context.addTestState(TEST_CONTEXT.SCREENSHOT_DIRECTORY, screenshotsDir.toString());
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS, new UserPersonaDetails());
        context.addTestState(TEST_CONTEXT.SOFT_ASSERTIONS, new SoftAssertions());
        context.addTestState(TEST_CONTEXT.SCREENSHOT_MANAGER, new ScreenShotManager());

        Driver driver = Drivers.createDriverFor("buyer", Platform.web, context);

        assertThat(driver.getInnerDriver()).isInstanceOf(PlaywrightWebDriver.class);
        SessionHandle sessionHandle = Drivers.getSessionHandleForCurrentUser(Thread.currentThread().getId());
        assertThat(sessionHandle.engine()).isEqualTo(WebEngine.PLAYWRIGHT_TS.getConfigValue());
        assertThat(sessionHandle.metadata()).containsEntry("browserName", "chrome");
        assertThat(sessionHandle.metadata()).containsKey("contextId");
        assertThat(sessionHandle.metadata()).containsKey("pageId");
        assertThat(sessionHandle.metadata()).containsKey("workerSessionId");
    }
}
