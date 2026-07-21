package com.znsio.teswiz.runner;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.web.WebEngine;
import com.znsio.teswiz.web.playwright.PlaywrightWebDriver;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerManager;
import com.znsio.teswiz.web.selenium.BrowserDriverManager;

final class WebSessionFactory {
    private static final PlaywrightWorkerManager PLAYWRIGHT_WORKER_MANAGER = new PlaywrightWorkerManager();

    private WebSessionFactory() {
    }

    static Driver createWebDriverForUser(String userPersona, String browserName, Platform forPlatform,
            TestExecutionContext context) {
        return createWebDriverForUser(userPersona, browserName, forPlatform, context, PLAYWRIGHT_WORKER_MANAGER);
    }

    static Driver createWebDriverForUser(String userPersona, String browserName, Platform forPlatform,
            TestExecutionContext context, PlaywrightWorkerManager playwrightWorkerManager) {
        WebEngine webEngine = Runner.getWebEngine();
        String runningOn = Runner.isRunningInCI() ? "CI" : "local";
        context.addTestState(TEST_CONTEXT.WEB_BROWSER_ON, runningOn);
        switch (webEngine) {
            case SELENIUM:
                return BrowserDriverManager.createWebDriverForUser(userPersona, browserName, forPlatform, context);
            case PLAYWRIGHT_TS:
                PlaywrightWorkerManager.ManagedPlaywrightSession managedSession = playwrightWorkerManager
                        .createManagedSession(userPersona, browserName, forPlatform, context);
                context.addTestState(TEST_CONTEXT.ENGINE_SESSION_HANDLE, managedSession.sessionHandle());
                Drivers.addUserPersonaDriverCapabilities(userPersona, managedSession.createCapabilities());
                PlaywrightWebDriver playwrightWebDriver = managedSession.createWebDriver();
                playwrightWebDriver.get(BrowserDriverManager.getBaseUrlFor(userPersona));
                return new Driver(context.getTestName() + "-" + userPersona, forPlatform, userPersona,
                        Drivers.getAppNamefor(userPersona), playwrightWebDriver, Runner.isRunningInHeadlessMode());
            default:
                throw new InvalidTestDataException(
                        String.format("Unexpected web engine: '%s'", webEngine.getConfigValue()));
        }
    }
}
