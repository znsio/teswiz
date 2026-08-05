package com.znsio.teswiz.web.browser;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.session.SessionHandle;
import com.znsio.teswiz.web.WebEngine;
import com.znsio.teswiz.web.playwright.PlaywrightJavaDriverManager;
import com.znsio.teswiz.web.playwright.PlaywrightWebDriver;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerManager;
import com.znsio.teswiz.web.provider.playwright.PlaywrightCloudSessionMetadataResolver;
import com.znsio.teswiz.web.selenium.SeleniumDriverManager;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BrowserDriverManager {
    private static final PlaywrightWorkerManager PLAYWRIGHT_WORKER_MANAGER = new PlaywrightWorkerManager();
    private static final PlaywrightJavaDriverManager PLAYWRIGHT_JAVA_DRIVER_MANAGER = new PlaywrightJavaDriverManager();
    private static final PlaywrightCloudSessionMetadataResolver PLAYWRIGHT_CLOUD_SESSION_METADATA_RESOLVER =
            new PlaywrightCloudSessionMetadataResolver();

    private BrowserDriverManager() {
    }

    public static WebDriverSessionResult createWebSessionForUser(String userPersona, String browserName,
            Platform forPlatform, TestExecutionContext context) {
        return createWebSessionForUser(userPersona, browserName, forPlatform, context, PLAYWRIGHT_WORKER_MANAGER,
                PLAYWRIGHT_JAVA_DRIVER_MANAGER);
    }

    static WebDriverSessionResult createWebSessionForUser(String userPersona, String browserName,
            Platform forPlatform, TestExecutionContext context, PlaywrightWorkerManager playwrightWorkerManager,
            PlaywrightJavaDriverManager playwrightJavaDriverManager) {
        return createWebSessionForUser(userPersona, browserName, forPlatform, context, playwrightWorkerManager,
                playwrightJavaDriverManager, PLAYWRIGHT_CLOUD_SESSION_METADATA_RESOLVER);
    }

    static WebDriverSessionResult createWebSessionForUser(String userPersona, String browserName,
            Platform forPlatform, TestExecutionContext context, PlaywrightWorkerManager playwrightWorkerManager,
            PlaywrightJavaDriverManager playwrightJavaDriverManager,
            PlaywrightCloudSessionMetadataResolver cloudSessionMetadataResolver) {
        WebEngine webEngine = Runner.getWebEngine();
        String runningOn = Runner.isRunningInCI() ? "CI" : "local";
        context.addTestState(TEST_CONTEXT.WEB_BROWSER_ON, runningOn);
        switch (webEngine) {
            case SELENIUM:
                return SeleniumDriverManager.createWebSessionForUser(userPersona, browserName, forPlatform, context);
            case PLAYWRIGHT_JAVA:
                return playwrightJavaDriverManager.createWebSessionForUser(userPersona, browserName, forPlatform,
                        context);
            case PLAYWRIGHT_TS:
                return createPlaywrightWebSessionForUser(userPersona, browserName, forPlatform, context,
                        playwrightWorkerManager, cloudSessionMetadataResolver);
            default:
                throw new InvalidTestDataException(
                        String.format("Unexpected web engine: '%s'", webEngine.getConfigValue()));
        }
    }

    public static void closeWebDriver(String userPersona, Driver driver) {
        switch (Runner.getWebEngine()) {
            case SELENIUM:
                SeleniumDriverManager.closeWebDriver(userPersona, driver);
                break;
            case PLAYWRIGHT_JAVA:
                PLAYWRIGHT_JAVA_DRIVER_MANAGER.closeWebDriver(userPersona, driver);
                break;
            case PLAYWRIGHT_TS:
                closePlaywrightWebDriver(driver);
                break;
            default:
                throw new InvalidTestDataException(
                        String.format("Unexpected web engine: '%s'", Runner.getWebEngine().getConfigValue()));
        }
    }

    public static Driver createElectronDriverForUser(String userPersona, String browserName,
            Platform forPlatform, TestExecutionContext context) {
        return SeleniumDriverManager.createElectronDriverForUser(userPersona, browserName, forPlatform, context);
    }

    private static void closePlaywrightWebDriver(Driver driver) {
        if (null != driver.getInnerDriver()) {
            driver.getInnerDriver().quit();
        }
    }

    private static WebDriverSessionResult createPlaywrightWebSessionForUser(String userPersona, String browserName,
            Platform forPlatform, TestExecutionContext context,
            PlaywrightWorkerManager playwrightWorkerManager,
            PlaywrightCloudSessionMetadataResolver cloudSessionMetadataResolver) {
        PlaywrightWorkerManager.ManagedPlaywrightSession managedSession = playwrightWorkerManager
                .createManagedSession(userPersona, browserName, forPlatform, context);
        PlaywrightWebDriver playwrightWebDriver = managedSession.createWebDriver();
        playwrightWebDriver.get(com.znsio.teswiz.web.selenium.WebBaseUrlResolver.resolve(
                Drivers.getAppNamefor(userPersona)));
        SessionHandle sessionHandle = enrichSessionHandle(managedSession.sessionHandle(),
                cloudSessionMetadataResolver.resolve(playwrightWebDriver,
                        managedSession.sessionHandle().metadata().get("provider")));
        return new WebDriverSessionResult(playwrightWebDriver,
                Runner.isRunningInHeadlessMode(),
                managedSession.createCapabilities(),
                sessionHandle);
    }

    private static SessionHandle enrichSessionHandle(SessionHandle sessionHandle, Map<String, String> additionalMetadata) {
        if (additionalMetadata.isEmpty()) {
            return sessionHandle;
        }
        Map<String, String> mergedMetadata = new LinkedHashMap<>(sessionHandle.metadata());
        mergedMetadata.putAll(additionalMetadata);
        return new SessionHandle(sessionHandle.userPersona(), sessionHandle.platform(), sessionHandle.engine(),
                sessionHandle.sessionId(), sessionHandle.artifactPath(), mergedMetadata);
    }
}
