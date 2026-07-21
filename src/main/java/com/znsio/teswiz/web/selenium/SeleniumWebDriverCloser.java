package com.znsio.teswiz.web.selenium;

import com.znsio.teswiz.runner.Driver;

public final class SeleniumWebDriverCloser {
    private SeleniumWebDriverCloser() {
    }

    public static void closeWebDriver(String userPersona, Driver driver) {
        BrowserDriverManager.closeWebDriver(userPersona, driver);
    }
}
