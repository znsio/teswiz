package com.znsio.teswiz.runner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

final class PlaywrightDriverManager {
    private static final Logger LOGGER = LogManager.getLogger(PlaywrightDriverManager.class.getName());

    private PlaywrightDriverManager() {
    }

    static void closeWebDriver(String userPersona, @NotNull Driver driver) {
        LOGGER.info("Closing Playwright web driver for user: '{}'", userPersona);
        if (null != driver.getInnerDriver()) {
            driver.getInnerDriver().quit();
        }
    }
}
