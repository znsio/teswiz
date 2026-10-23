package com.znsio.teswiz.web.playwright;

import java.util.concurrent.atomic.AtomicInteger;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;

final class PlaywrightJavaRuntime {
    private final Playwright playwright;
    private final Browser browser;
    private final AtomicInteger activeSessions = new AtomicInteger();

    PlaywrightJavaRuntime(Playwright playwright, Browser browser) {
        this.playwright = playwright;
        this.browser = browser;
    }

    Playwright playwright() {
        return playwright;
    }

    Browser browser() {
        return browser;
    }

    int incrementSessions() {
        return activeSessions.incrementAndGet();
    }

    int decrementSessions() {
        return activeSessions.decrementAndGet();
    }
}
