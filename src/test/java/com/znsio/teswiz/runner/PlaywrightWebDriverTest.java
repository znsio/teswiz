package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Rectangle;

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
}
