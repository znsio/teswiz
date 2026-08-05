package com.znsio.teswiz.web.playwright;

import java.nio.file.Path;
import java.util.List;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.znsio.teswiz.config.browser.PlaywrightBrowserConfig;

record PlaywrightJavaSession(
        String sessionId,
        String userPersona,
        PlaywrightJavaRuntime runtime,
        PlaywrightBrowserConfig browserConfig,
        BrowserContext browserContext,
        Page page,
        Path traceFile,
        Path harFile,
        Path consoleFile,
        List<String> consoleMessages) {
}
