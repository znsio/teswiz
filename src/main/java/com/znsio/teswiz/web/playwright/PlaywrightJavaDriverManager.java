package com.znsio.teswiz.web.playwright;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.web.browser.WebDriverSessionResult;

public final class PlaywrightJavaDriverManager {
    private static final String NOT_IMPLEMENTED_MESSAGE =
            "WEB_ENGINE=playwright-java is recognized, but the Playwright Java runtime is not implemented yet.";

    private PlaywrightJavaDriverManager() {
    }

    public static WebDriverSessionResult createWebSessionForUser(String userPersona, String browserName, Platform forPlatform,
            TestExecutionContext context) {
        throw new InvalidTestDataException(NOT_IMPLEMENTED_MESSAGE);
    }

    public static void closeWebDriver(String userPersona, Driver driver) {
        if (null != driver.getInnerDriver()) {
            driver.getInnerDriver().quit();
        }
    }
}
