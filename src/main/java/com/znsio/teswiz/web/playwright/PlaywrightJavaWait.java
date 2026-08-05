package com.znsio.teswiz.web.playwright;

import java.time.Duration;
import java.time.Instant;

import com.microsoft.playwright.Locator;

final class PlaywrightJavaWait {
    private static final Duration POLL_INTERVAL = Duration.ofMillis(100);

    private PlaywrightJavaWait() {
    }

    static int untilCountAtLeast(Locator locator, Duration timeout, int expectedMinimum) {
        Instant deadline = Instant.now().plus(timeout);
        int currentCount = locator.count();
        while (currentCount < expectedMinimum && Instant.now().isBefore(deadline)) {
            sleep();
            currentCount = locator.count();
        }
        return currentCount;
    }

    private static void sleep() {
        try {
            Thread.sleep(POLL_INTERVAL.toMillis());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for Playwright Java element lookup",
                    interruptedException);
        }
    }
}
