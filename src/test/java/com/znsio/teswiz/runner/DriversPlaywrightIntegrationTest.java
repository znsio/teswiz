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
import com.znsio.teswiz.web.playwright.PlaywrightWorkerClient;
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

        TestExecutionContext context = createPlaywrightContext("playwright-driver-integration");

        Driver driver = Drivers.createDriverFor("buyer", Platform.web, context);

        assertThat(driver.getInnerDriver()).isInstanceOf(PlaywrightWebDriver.class);
        SessionHandle sessionHandle = Drivers.getSessionHandleForCurrentUser(Thread.currentThread().getId());
        assertThat(sessionHandle.engine()).isEqualTo(WebEngine.PLAYWRIGHT_TS.getConfigValue());
        assertThat(sessionHandle.metadata()).containsEntry("browserName", "chrome");
        assertThat(sessionHandle.metadata()).containsEntry("provider", "local");
        assertThat(sessionHandle.metadata()).containsKey("contextId");
        assertThat(sessionHandle.metadata()).containsKey("pageId");
        assertThat(sessionHandle.metadata()).containsKey("workerSessionId");
    }

    @Test
    void shouldKeepPlaywrightPersonasIsolatedWithinSameScenario() throws Exception {
        System.setProperty("WEB_ENGINE", "playwright-ts");
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);

        TestExecutionContext context = createPlaywrightContext("playwright-multi-user-integration");
        Path sharedHtml = writeSharedPersonaPage();

        Drivers.createDriverFor("buyer", Platform.web, context);
        SessionHandle buyerSessionHandle = Drivers.getSessionHandleForCurrentUser(Thread.currentThread().getId());
        PlaywrightWebDriver buyerDriver = (PlaywrightWebDriver) Drivers.getDriverForCurrentUser(Thread.currentThread().getId())
                .getInnerDriver();

        Drivers.createDriverFor("seller", Platform.web, context);
        SessionHandle sellerSessionHandle = Drivers.getSessionHandleForCurrentUser(Thread.currentThread().getId());
        PlaywrightWebDriver sellerDriver = (PlaywrightWebDriver) Drivers.getDriverForCurrentUser(Thread.currentThread().getId())
                .getInnerDriver();

        buyerDriver.get(sharedHtml.toUri().toString());
        sellerDriver.get(sharedHtml.toUri().toString());

        buyerDriver.executeScript("window.personaLabel = arguments[0];", "buyer");
        sellerDriver.executeScript("window.personaLabel = arguments[0];", "seller");

        assertThat(buyerDriver.executeScript("return window.personaLabel;")).isEqualTo("buyer");
        assertThat(sellerDriver.executeScript("return window.personaLabel;")).isEqualTo("seller");

        buyerDriver.switchTo().newWindow(org.openqa.selenium.WindowType.TAB);
        assertThat(buyerDriver.getWindowHandles()).hasSize(2);
        assertThat(sellerDriver.getWindowHandles()).hasSize(1);

        PlaywrightWorkerClient workerClient = (PlaywrightWorkerClient) context
                .getTestState(TEST_CONTEXT.PLAYWRIGHT_WORKER_CLIENT);
        assertThat(workerClient).isNotNull();
        assertThat(buyerSessionHandle.metadata().get("contextId"))
                .isNotEqualTo(sellerSessionHandle.metadata().get("contextId"));
        assertThat(buyerSessionHandle.metadata().get("workerSessionId"))
                .isNotEqualTo(sellerSessionHandle.metadata().get("workerSessionId"));
    }

    @Test
    void shouldReassignPersonaWithoutLosingUnderlyingPlaywrightSession() throws Exception {
        System.setProperty("WEB_ENGINE", "playwright-ts");
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);

        TestExecutionContext context = createPlaywrightContext("playwright-persona-reassign");
        Drivers.createDriverFor("buyer", Platform.web, context);

        SessionHandle originalSessionHandle = Drivers.getSessionHandleForCurrentUser(Thread.currentThread().getId());
        Driver originalDriver = Drivers.getDriverForCurrentUser(Thread.currentThread().getId());

        Drivers.assignNewPersonaToExistingDriver("buyer", "approver", context);
        Driver reassignedDriver = Drivers.setDriverFor("approver", Platform.web, context);
        SessionHandle reassignedSessionHandle = Drivers.getSessionHandleForCurrentUser(Thread.currentThread().getId());

        assertThat(reassignedDriver).isSameAs(originalDriver);
        assertThat(reassignedSessionHandle.sessionId()).isEqualTo(originalSessionHandle.sessionId());
        assertThat(reassignedSessionHandle.metadata()).isEqualTo(originalSessionHandle.metadata());
        assertThat(reassignedSessionHandle.userPersona()).isEqualTo("approver");
        assertThat(Drivers.getAvailableUserPersonas()).contains("approver").doesNotContain("buyer");
    }

    private TestExecutionContext createPlaywrightContext(String testName) throws Exception {
        TestExecutionContext context = new TestExecutionContext(testName);
        Path scenarioDir = Files.createTempDirectory(testName);
        Path screenshotsDir = Files.createDirectory(scenarioDir.resolve("screenshots"));
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, scenarioDir.toString());
        context.addTestState(TEST_CONTEXT.SCREENSHOT_DIRECTORY, screenshotsDir.toString());
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS, new UserPersonaDetails());
        context.addTestState(TEST_CONTEXT.SOFT_ASSERTIONS, new SoftAssertions());
        context.addTestState(TEST_CONTEXT.SCREENSHOT_MANAGER, new ScreenShotManager());
        return context;
    }

    private Path writeSharedPersonaPage() throws Exception {
        String html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="UTF-8" />
                  <title>Persona Isolation</title>
                </head>
                <body>
                  <h1 id="title">Persona Isolation</h1>
                </body>
                </html>
                """;
        Path file = Files.createTempFile("playwright-persona-", ".html");
        Files.writeString(file, html);
        return file;
    }
}
