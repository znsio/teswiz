package com.znsio.teswiz.runner;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.InvalidTestDataException;

final class WebSessionFactory {
    private static final PlaywrightWorkerManager PLAYWRIGHT_WORKER_MANAGER = new PlaywrightWorkerManager();

    private WebSessionFactory() {
    }

    static Driver createWebDriverForUser(String userPersona, String browserName, Platform forPlatform,
            TestExecutionContext context) {
        WebEngine webEngine = Runner.getWebEngine();
        switch (webEngine) {
            case SELENIUM:
                return BrowserDriverManager.createWebDriverForUser(userPersona, browserName, forPlatform, context);
            case PLAYWRIGHT_TS:
                PlaywrightWorkerManager.ManagedPlaywrightSession managedSession = PLAYWRIGHT_WORKER_MANAGER
                        .createManagedSession(userPersona, browserName, forPlatform, context);
                context.addTestState(TEST_CONTEXT.ENGINE_SESSION_HANDLE, managedSession.sessionHandle());
                Drivers.addUserPersonaDriverCapabilities(userPersona, managedSession.createCapabilities());
                return new Driver(context.getTestName() + "-" + userPersona, forPlatform, userPersona,
                        Drivers.getAppNamefor(userPersona), managedSession.createWebDriver(), false);
            default:
                throw new InvalidTestDataException(
                        String.format("Unexpected web engine: '%s'", webEngine.getConfigValue()));
        }
    }
}
