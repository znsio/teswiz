package com.znsio.teswiz.runner;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.web.browser.BrowserDriverManager;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerManager;

final class WebSessionFactory {
    private WebSessionFactory() {
    }

    static Driver createWebDriverForUser(String userPersona, String browserName, Platform forPlatform,
            TestExecutionContext context) {
        return BrowserDriverManager.createWebDriverForUser(userPersona, browserName, forPlatform, context);
    }

    static Driver createWebDriverForUser(String userPersona, String browserName, Platform forPlatform,
            TestExecutionContext context, PlaywrightWorkerManager playwrightWorkerManager) {
        return BrowserDriverManager.createWebDriverForUser(userPersona, browserName, forPlatform, context,
                playwrightWorkerManager);
    }
}
