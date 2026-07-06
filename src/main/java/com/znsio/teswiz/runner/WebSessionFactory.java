package com.znsio.teswiz.runner;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.exceptions.InvalidTestDataException;

final class WebSessionFactory {
    private WebSessionFactory() {
    }

    static Driver createWebDriverForUser(String userPersona, String browserName, Platform forPlatform,
            TestExecutionContext context) {
        WebEngine webEngine = Runner.getWebEngine();
        switch (webEngine) {
            case SELENIUM:
                return BrowserDriverManager.createWebDriverForUser(userPersona, browserName, forPlatform, context);
            case PLAYWRIGHT_TS:
                throw new InvalidTestDataException(
                        "WEB_ENGINE=playwright-ts is configured, but the Playwright TS worker is not implemented yet");
            default:
                throw new InvalidTestDataException(
                        String.format("Unexpected web engine: '%s'", webEngine.getConfigValue()));
        }
    }
}
