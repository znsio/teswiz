package com.znsio.teswiz.web.playwright.screen;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.web.playwright.PlaywrightJavaWebDriver;

public record PlaywrightJavaScreenContext(
        Driver driver,
        Visual visual,
        PlaywrightJavaWebDriver webDriver,
        BrowserContext browserContext,
        Page page) {
}
