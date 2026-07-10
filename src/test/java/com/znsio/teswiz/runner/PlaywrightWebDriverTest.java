package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import com.znsio.teswiz.web.playwright.PlaywrightWebDriver;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerClient;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerSession;

class PlaywrightWebDriverTest {
    private PlaywrightWorkerClient workerClient;

    @AfterEach
    void tearDown() {
        if (null != workerClient) {
            workerClient.close();
        }
    }

    @Test
    void shouldNavigateCapturePageSourceAndTakeScreenshot() throws Exception {
        Path htmlFile = writeTestPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("buyer", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        SharedWebDriverContract.assertNavigationPageSourceAndScreenshot(driver, htmlFile);
    }

    @Test
    void shouldFindElementsAndPerformCoreInteractions() throws Exception {
        Path htmlFile = writeTestPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("seller", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        SharedWebDriverContract.assertCoreInteractions(driver, htmlFile);
    }

    @Test
    void shouldExecuteJavascriptWithElementAndLiteralArguments() throws Exception {
        Path htmlFile = writeTestPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("host", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        SharedWebDriverContract.assertJavascriptExecution(driver, htmlFile);
    }

    @Test
    void shouldOpenAndSwitchBetweenTabs() throws Exception {
        Path htmlFile = writeTestPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("guest", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        SharedWebDriverContract.assertTabHandling(driver, htmlFile);
    }

    @Test
    void shouldSwitchIntoFrameAndBackToDefaultContent() throws Exception {
        Path htmlFile = writeFrameTestPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("frame-user", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        SharedWebDriverContract.assertFrameHandling(driver, htmlFile);
    }

    @Test
    void shouldSwitchIntoFrameUsingWebElement() throws Exception {
        Path htmlFile = writeFrameTestPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("frame-element-user", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        driver.get(htmlFile.toUri().toString());
        org.openqa.selenium.WebElement frameElement = driver.findElement(By.id("details-frame"));
        driver.switchTo().frame(frameElement);

        assertThat(driver.findElement(By.id("inside")).getText()).isEqualTo("Inside Frame");
        driver.switchTo().defaultContent();
        assertThat(driver.findElement(By.id("outside")).getText()).isEqualTo("Outside Frame");
    }

    @Test
    void shouldAccessOpenShadowDomThroughShadowRootSearchContext() throws Exception {
        Path htmlFile = writeShadowDomTestPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("shadow-user", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        SharedWebDriverContract.assertOpenShadowDomHandling(driver, htmlFile);
    }

    @Test
    void shouldSupportWindowSizingApis() throws Exception {
        Path htmlFile = writeTestPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("window-user", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        SharedWebDriverContract.assertWindowHandling(driver, htmlFile);
    }

    @Test
    void shouldHandleAlertsAndPrompts() throws Exception {
        Path htmlFile = writeAlertTestPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("alert-user", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        SharedWebDriverContract.assertAlertHandling(driver, htmlFile);
    }

    @Test
    void shouldReturnFocusedElementAsActiveElement() throws Exception {
        Path htmlFile = writeTestPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("focus-user", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        SharedWebDriverContract.assertActiveElementHandling(driver, htmlFile);
    }

    @Test
    void shouldManageCookies() throws Exception {
        try (SharedWebDriverContract.LocalCookieServer server = new SharedWebDriverContract.LocalCookieServer()) {
            workerClient = new PlaywrightWorkerClient();
            workerClient.start();
            PlaywrightWorkerSession session = workerClient.createSession("cookie-user", "chromium");
            PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

            SharedWebDriverContract.assertCookieHandling(driver, server.url());
        }
    }

    @Test
    void shouldHonorImplicitWaitAndExposeConfiguredTimeouts() throws Exception {
        Path htmlFile = writeDelayedElementPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("timeout-user", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        SharedWebDriverContract.assertImplicitWaitAndTimeoutHandling(driver, htmlFile);
    }

    @Test
    void shouldExposeBrowserLogsThroughManageLogs() throws Exception {
        Path htmlFile = writeConsoleLogPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("logs-user", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        SharedWebDriverContract.assertBrowserLogs(driver, htmlFile);
    }

    private Path writeTestPage() throws Exception {
        String html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="UTF-8" />
                  <title>Playwright Bridge</title>
                </head>
                <body>
                  <h1 id="title">Playwright Bridge</h1>
                  <input id="name" />
                  <button id="save" onclick="document.getElementById('status').innerText = 'Saved ' + document.getElementById('name').value;">Save</button>
                  <div id="status">Idle</div>
                  <ul>
                    <li class="item">one</li>
                    <li class="item">two</li>
                  </ul>
                </body>
                </html>
                """;
        Path file = Files.createTempFile("playwright-bridge-", ".html");
        Files.writeString(file, html);
        return file;
    }

    private Path writeFrameTestPage() throws Exception {
        String html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="UTF-8" />
                  <title>Playwright Frame Bridge</title>
                </head>
                <body>
                  <div id="outside">Outside Frame</div>
                  <iframe id="details-frame" name="details-frame"
                    srcdoc="<html><body><div id='inside'>Inside Frame</div></body></html>"></iframe>
                </body>
                </html>
                """;
        Path file = Files.createTempFile("playwright-frame-bridge-", ".html");
        Files.writeString(file, html);
        return file;
    }

    private Path writeAlertTestPage() throws Exception {
        String html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="UTF-8" />
                  <title>Playwright Alert Bridge</title>
                </head>
                <body>
                  <button id="alertButton" onclick="setTimeout(() => { alert('Plain alert'); document.getElementById('result').innerText='alert-done'; }, 0);">Alert</button>
                  <button id="promptButton" onclick="setTimeout(() => { const value = prompt('Enter your name', 'Guest'); document.getElementById('result').innerText='prompt:' + value; }, 0);">Prompt</button>
                  <div id="result">idle</div>
                </body>
                </html>
                """;
        Path file = Files.createTempFile("playwright-alert-bridge-", ".html");
        Files.writeString(file, html);
        return file;
    }

    private Path writeShadowDomTestPage() throws Exception {
        String html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="UTF-8" />
                  <title>Playwright Shadow Bridge</title>
                </head>
                <body>
                  <div id="shadow-host"></div>
                  <script>
                    const host = document.getElementById('shadow-host');
                    const root = host.attachShadow({ mode: 'open' });
                    root.innerHTML = `
                      <div id="shadow-text">Inside Shadow Root</div>
                      <button data-testid="shadow-button" onclick="this.getRootNode().getElementById('shadow-status').textContent = 'clicked'">Press</button>
                      <div id="shadow-status">idle</div>
                      <span class="shadow-item">one</span>
                      <span class="shadow-item">two</span>
                    `;
                  </script>
                </body>
                </html>
                """;
        Path file = Files.createTempFile("playwright-shadow-bridge-", ".html");
        Files.writeString(file, html);
        return file;
    }

    private Path writeDelayedElementPage() throws Exception {
        String html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="UTF-8" />
                  <title>Playwright Timeout Bridge</title>
                  <script>
                    window.setTimeout(() => {
                      const element = document.createElement('div');
                      element.id = 'delayed';
                      element.innerText = 'Ready Later';
                      document.body.appendChild(element);
                    }, 300);
                  </script>
                </head>
                <body>
                  <h1 id="title">Timeout Bridge</h1>
                </body>
                </html>
                """;
        Path file = Files.createTempFile("playwright-timeout-bridge-", ".html");
        Files.writeString(file, html);
        return file;
    }

    private Path writeConsoleLogPage() throws Exception {
        String html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="UTF-8" />
                  <title>Playwright Log Bridge</title>
                  <script>
                    console.log('playwright-browser-log');
                    console.warn('playwright-browser-warning');
                  </script>
                </head>
                <body>
                  <h1 id="title">Log Bridge</h1>
                </body>
                </html>
                """;
        Path file = Files.createTempFile("playwright-log-bridge-", ".html");
        Files.writeString(file, html);
        return file;
    }

}
