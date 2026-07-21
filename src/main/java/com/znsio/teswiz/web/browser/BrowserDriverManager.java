package com.znsio.teswiz.web.browser;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.web.WebEngine;
import com.znsio.teswiz.web.playwright.PlaywrightDriverManager;
import com.znsio.teswiz.web.playwright.PlaywrightWebDriver;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerManager;
import com.znsio.teswiz.web.selenium.SeleniumDriverManager;

public final class BrowserDriverManager {
    private static final PlaywrightWorkerManager PLAYWRIGHT_WORKER_MANAGER = new PlaywrightWorkerManager();

    private BrowserDriverManager() {
    }

    public static Driver createWebDriverForUser(String userPersona, String browserName,
            Platform forPlatform, TestExecutionContext context) {
        return createWebDriverForUser(userPersona, browserName, forPlatform, context, PLAYWRIGHT_WORKER_MANAGER);
    }

    static Driver createWebDriverForUser(String userPersona, String browserName,
            Platform forPlatform, TestExecutionContext context, PlaywrightWorkerManager playwrightWorkerManager) {
        WebEngine webEngine = Runner.getWebEngine();
        String runningOn = Runner.isRunningInCI() ? "CI" : "local";
        context.addTestState(TEST_CONTEXT.WEB_BROWSER_ON, runningOn);
        switch (webEngine) {
            case SELENIUM:
                return SeleniumDriverManager.createWebDriverForUser(userPersona, browserName, forPlatform, context);
            case PLAYWRIGHT_TS:
                return createPlaywrightWebDriverForUser(userPersona, browserName, forPlatform, context,
                        playwrightWorkerManager);
            default:
                throw new InvalidTestDataException(
                        String.format("Unexpected web engine: '%s'", webEngine.getConfigValue()));
        }
    }

    public static void closeWebDriver(String userPersona, Driver driver) {
        if (driver.getInnerDriver() instanceof PlaywrightWebDriver) {
            PlaywrightDriverManager.closeWebDriver(userPersona, driver);
        } else {
            SeleniumDriverManager.closeWebDriver(userPersona, driver);
        }
    }

    public static Driver createElectronDriverForUser(String userPersona, String browserName,
            Platform forPlatform, TestExecutionContext context) {
        return SeleniumDriverManager.createElectronDriverForUser(userPersona, browserName, forPlatform, context);
    }

    private static Driver createPlaywrightWebDriverForUser(String userPersona, String browserName,
            Platform forPlatform, TestExecutionContext context,
            PlaywrightWorkerManager playwrightWorkerManager) {
        PlaywrightWorkerManager.ManagedPlaywrightSession managedSession = playwrightWorkerManager
                .createManagedSession(userPersona, browserName, forPlatform, context);
        context.addTestState(TEST_CONTEXT.ENGINE_SESSION_HANDLE, managedSession.sessionHandle());
        Drivers.addUserPersonaDriverCapabilities(userPersona, managedSession.createCapabilities());
        PlaywrightWebDriver playwrightWebDriver = managedSession.createWebDriver();
        playwrightWebDriver.get(com.znsio.teswiz.web.selenium.WebBaseUrlResolver.resolve(
                Drivers.getAppNamefor(userPersona)));
        return new Driver(context.getTestName() + "-" + userPersona, forPlatform, userPersona,
                Drivers.getAppNamefor(userPersona), playwrightWebDriver, Runner.isRunningInHeadlessMode());
    }
}
