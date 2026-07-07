package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WindowType;

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

        driver.get(htmlFile.toUri().toString());

        assertThat(driver.getCurrentUrl()).contains(htmlFile.getFileName().toString());
        assertThat(driver.getPageSource()).contains("Playwright Bridge");
        byte[] screenshot = driver.getScreenshotAs(OutputType.BYTES);
        assertThat(screenshot).isNotEmpty();
    }

    @Test
    void shouldFindElementsAndPerformCoreInteractions() throws Exception {
        Path htmlFile = writeTestPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("seller", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        driver.get(htmlFile.toUri().toString());
        driver.findElement(By.id("name")).sendKeys("Anand");
        driver.findElement(By.id("save")).click();

        assertThat(driver.findElement(By.id("name")).getAttribute("value")).isEqualTo("Anand");
        assertThat(driver.findElement(By.id("status")).getText()).isEqualTo("Saved Anand");
        assertThat(driver.findElements(By.cssSelector(".item"))).hasSize(2);
        assertThat(driver.findElement(By.id("status")).isDisplayed()).isTrue();
        Rectangle nameBounds = driver.findElement(By.id("name")).getRect();
        assertThat(nameBounds.getWidth()).isPositive();
        assertThat(nameBounds.getHeight()).isPositive();
    }

    @Test
    void shouldExecuteJavascriptWithElementAndLiteralArguments() throws Exception {
        Path htmlFile = writeTestPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("host", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        driver.get(htmlFile.toUri().toString());
        driver.findElement(By.id("name")).sendKeys("Teswiz");
        driver.executeScript("arguments[0].click()", driver.findElement(By.id("save")));

        String statusText = (String) driver.executeScript(
                "return arguments[0].innerText",
                driver.findElement(By.id("status")));
        assertThat(statusText).isEqualTo("Saved Teswiz");

        driver.executeScript(
                "arguments[0].setAttribute(arguments[1], arguments[2])",
                driver.findElement(By.id("status")),
                "data-engine",
                "playwright-ts");
        assertThat(driver.findElement(By.id("status")).getAttribute("data-engine")).isEqualTo("playwright-ts");
    }

    @Test
    void shouldOpenAndSwitchBetweenTabs() throws Exception {
        Path htmlFile = writeTestPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("guest", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        driver.get(htmlFile.toUri().toString());
        String originalHandle = driver.getWindowHandle();

        driver.switchTo().newWindow(WindowType.TAB);
        String newHandle = driver.getWindowHandle();
        assertThat(newHandle).isNotEqualTo(originalHandle);
        assertThat(driver.getWindowHandles()).contains(originalHandle, newHandle);

        driver.get("data:text/html,<title>Second Tab</title><div id='tab'>tab-2</div>");
        assertThat(driver.getTitle()).isEqualTo("Second Tab");

        driver.switchTo().window(originalHandle);
        assertThat(driver.getWindowHandle()).isEqualTo(originalHandle);
        assertThat(driver.getTitle()).isEqualTo("Playwright Bridge");

        driver.switchTo().window(newHandle);
        assertThat(driver.findElement(By.id("tab")).getText()).isEqualTo("tab-2");
    }

    @Test
    void shouldSwitchIntoFrameAndBackToDefaultContent() throws Exception {
        Path htmlFile = writeFrameTestPage();
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        PlaywrightWorkerSession session = workerClient.createSession("frame-user", "chromium");
        PlaywrightWebDriver driver = new PlaywrightWebDriver(workerClient, session);

        driver.get(htmlFile.toUri().toString());
        assertThat(driver.findElement(By.id("outside")).getText()).isEqualTo("Outside Frame");

        driver.switchTo().frame("details-frame");
        assertThat(driver.findElement(By.id("inside")).getText()).isEqualTo("Inside Frame");
        driver.executeScript("arguments[0].setAttribute(arguments[1], arguments[2])",
                driver.findElement(By.id("inside")), "data-scope", "frame");
        assertThat(driver.findElement(By.id("inside")).getAttribute("data-scope")).isEqualTo("frame");

        driver.switchTo().defaultContent();
        assertThat(driver.findElement(By.id("outside")).getText()).isEqualTo("Outside Frame");
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
}
