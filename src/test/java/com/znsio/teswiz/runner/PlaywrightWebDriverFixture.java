package com.znsio.teswiz.runner;

import com.znsio.teswiz.web.playwright.PlaywrightWebDriver;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerClient;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerSession;
import org.openqa.selenium.WebDriver;

final class PlaywrightWebDriverFixture implements SharedWebDriverFixture {
    private final PlaywrightWorkerClient workerClient;

    PlaywrightWebDriverFixture() {
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
    }

    @Override
    public WebDriver createDriver(String userPersona) {
        PlaywrightWorkerSession session = workerClient.createSession(userPersona, "chromium");
        return new PlaywrightWebDriver(workerClient, session);
    }

    @Override
    public void close() {
        workerClient.close();
    }
}
