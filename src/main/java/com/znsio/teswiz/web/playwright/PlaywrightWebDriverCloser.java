package com.znsio.teswiz.web.playwright;

import com.znsio.teswiz.runner.Driver;

public final class PlaywrightWebDriverCloser {
    private PlaywrightWebDriverCloser() {
    }

    public static void closeWebDriver(String userPersona, Driver driver) {
        PlaywrightDriverManager.closeWebDriver(userPersona, driver);
    }
}
