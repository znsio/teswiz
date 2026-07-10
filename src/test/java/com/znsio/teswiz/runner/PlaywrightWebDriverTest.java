package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Date;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
        try (LocalHttpServer server = new LocalHttpServer()) {
            workerClient = new PlaywrightWorkerClient();
            workerClient.start();
            PlaywrightWorkerSession session = workerClient.createSession("cookie-user", "chromium");
            PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

            driver.get(server.url("/cookies"));

            Cookie sessionCookie = new Cookie.Builder("sessionId", "abc123")
                    .path("/")
                    .domain("127.0.0.1")
                    .expiresOn(new Date(System.currentTimeMillis() + 60_000))
                    .isHttpOnly(true)
                    .build();
            driver.manage().addCookie(sessionCookie);

            Cookie preferenceCookie = new Cookie.Builder("theme", "dark")
                    .path("/")
                    .domain("127.0.0.1")
                    .build();
            driver.manage().addCookie(preferenceCookie);

            assertThat(driver.manage().getCookieNamed("sessionId")).isNotNull();
            assertThat(driver.manage().getCookieNamed("sessionId").getValue()).isEqualTo("abc123");
            assertThat(driver.manage().getCookies())
                    .extracting(Cookie::getName)
                    .contains("sessionId", "theme");

            driver.manage().deleteCookieNamed("sessionId");
            assertThat(driver.manage().getCookieNamed("sessionId")).isNull();

            driver.manage().deleteCookie(preferenceCookie);
            assertThat(driver.manage().getCookieNamed("theme")).isNull();

            driver.manage().addCookie(new Cookie("region", "apac"));
            assertThat(driver.manage().getCookieNamed("region")).isNotNull();

            driver.manage().deleteAllCookies();
            assertThat(driver.manage().getCookies()).isEmpty();
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

    private static final class LocalHttpServer implements AutoCloseable {
        private final HttpServer server;

        private LocalHttpServer() throws IOException {
            server = HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/cookies", this::handleCookies);
            server.start();
        }

        private void handleCookies(HttpExchange exchange) throws IOException {
            byte[] body = """
                    <!doctype html>
                    <html>
                    <head><meta charset="UTF-8" /><title>Cookie Bridge</title></head>
                    <body><div id="cookie-page">Cookie Bridge</div></body>
                    </html>
                    """.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        }

        private String url(String path) {
            return "http://127.0.0.1:" + server.getAddress().getPort() + path;
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}
