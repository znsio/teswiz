package com.znsio.teswiz.web.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.znsio.teswiz.config.browser.PlaywrightBrowserConfig;

record PlaywrightJavaSession(
        String sessionId,
        PlaywrightJavaRuntime runtime,
        PlaywrightBrowserConfig browserConfig,
        BrowserContext browserContext,
        Page page) {
}
