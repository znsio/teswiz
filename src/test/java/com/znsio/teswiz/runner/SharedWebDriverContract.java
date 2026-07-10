package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.nio.file.Path;
import java.time.Duration;

final class SharedWebDriverContract {
    private SharedWebDriverContract() {
    }

    static void assertNavigationPageSourceAndScreenshot(WebDriver driver, Path htmlFile) {
        driver.get(htmlFile.toUri().toString());

        assertThat(driver.getCurrentUrl()).contains(htmlFile.getFileName().toString());
        assertThat(driver.getPageSource()).contains("Playwright Bridge");
        byte[] screenshot = ((org.openqa.selenium.TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        assertThat(screenshot).isNotEmpty();
    }

    static void assertCoreInteractions(WebDriver driver, Path htmlFile) {
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

    static void assertJavascriptExecution(WebDriver driver, Path htmlFile) {
        driver.get(htmlFile.toUri().toString());
        driver.findElement(By.id("name")).sendKeys("Teswiz");
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].click()", driver.findElement(By.id("save")));

        String statusText = (String) ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "return arguments[0].innerText",
                driver.findElement(By.id("status")));
        assertThat(statusText).isEqualTo("Saved Teswiz");

        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].setAttribute(arguments[1], arguments[2])",
                driver.findElement(By.id("status")),
                "data-engine",
                "playwright-ts");
        assertThat(driver.findElement(By.id("status")).getAttribute("data-engine")).isEqualTo("playwright-ts");
    }

    static void assertTabHandling(WebDriver driver, Path htmlFile) {
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

    static void assertFrameHandling(WebDriver driver, Path htmlFile) {
        driver.get(htmlFile.toUri().toString());
        assertThat(driver.findElement(By.id("outside")).getText()).isEqualTo("Outside Frame");

        driver.switchTo().frame("details-frame");
        assertThat(driver.findElement(By.id("inside")).getText()).isEqualTo("Inside Frame");
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "arguments[0].setAttribute(arguments[1], arguments[2])",
                driver.findElement(By.id("inside")), "data-scope", "frame");
        assertThat(driver.findElement(By.id("inside")).getAttribute("data-scope")).isEqualTo("frame");

        driver.switchTo().defaultContent();
        assertThat(driver.findElement(By.id("outside")).getText()).isEqualTo("Outside Frame");
    }

    static void assertAlertHandling(WebDriver driver, Path htmlFile) {
        driver.get(htmlFile.toUri().toString());
        driver.findElement(By.id("alertButton")).click();
        new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.alertIsPresent());
        org.openqa.selenium.Alert alert = driver.switchTo().alert();
        assertThat(alert.getText()).isEqualTo("Plain alert");
        alert.accept();
        assertThat(driver.findElement(By.id("result")).getText()).isEqualTo("alert-done");

        driver.findElement(By.id("promptButton")).click();
        new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.alertIsPresent());
        org.openqa.selenium.Alert prompt = driver.switchTo().alert();
        assertThat(prompt.getText()).isEqualTo("Enter your name");
        prompt.sendKeys("Teswiz");
        assertThat(driver.findElement(By.id("result")).getText()).isEqualTo("prompt:Teswiz");

        assertThatThrownBy(() -> driver.switchTo().alert().getText())
                .isInstanceOf(NoAlertPresentException.class);
    }

    static void assertActiveElementHandling(WebDriver driver, Path htmlFile) {
        driver.get(htmlFile.toUri().toString());
        driver.findElement(By.id("name")).click();

        org.openqa.selenium.WebElement activeElement = driver.switchTo().activeElement();
        assertThat(activeElement.getAttribute("id")).isEqualTo("name");

        activeElement.sendKeys("Focused");
        assertThat(driver.findElement(By.id("name")).getAttribute("value")).isEqualTo("Focused");
    }

    static void assertWindowHandling(WebDriver driver, Path htmlFile) {
        driver.get(htmlFile.toUri().toString());

        Dimension initialSize = driver.manage().window().getSize();
        assertThat(initialSize.getWidth()).isPositive();
        assertThat(initialSize.getHeight()).isPositive();

        driver.manage().window().setSize(new Dimension(900, 700));
        Dimension resized = driver.manage().window().getSize();
        assertThat(resized.getWidth()).isEqualTo(900);
        assertThat(resized.getHeight()).isEqualTo(700);

        driver.manage().window().setPosition(new Point(120, 80));
        Point position = driver.manage().window().getPosition();
        assertThat(position).isEqualTo(new Point(120, 80));

        driver.manage().window().minimize();
        Dimension minimized = driver.manage().window().getSize();
        assertThat(minimized.getWidth()).isLessThan(resized.getWidth());
        assertThat(minimized.getHeight()).isLessThan(resized.getHeight());

        driver.manage().window().fullscreen();
        Dimension fullscreen = driver.manage().window().getSize();
        assertThat(fullscreen.getWidth()).isGreaterThanOrEqualTo(minimized.getWidth());
        assertThat(fullscreen.getHeight()).isGreaterThanOrEqualTo(minimized.getHeight());

        driver.manage().window().maximize();
        Dimension maximized = driver.manage().window().getSize();
        assertThat(maximized.getWidth()).isGreaterThanOrEqualTo(fullscreen.getWidth());
        assertThat(maximized.getHeight()).isGreaterThanOrEqualTo(fullscreen.getHeight());
    }

    static void assertImplicitWaitAndTimeoutHandling(WebDriver driver, Path htmlFile) {
        driver.get(htmlFile.toUri().toString());
        driver.manage().timeouts()
                .implicitlyWait(Duration.ofMillis(900))
                .pageLoadTimeout(Duration.ofSeconds(12))
                .scriptTimeout(Duration.ofSeconds(7));

        long start = System.nanoTime();
        assertThat(driver.findElement(By.id("delayed")).getText()).isEqualTo("Ready Later");
        Duration successfulWait = Duration.ofNanos(System.nanoTime() - start);

        assertThat(successfulWait.toMillis()).isGreaterThanOrEqualTo(250L);
        assertThat(driver.manage().timeouts().getImplicitWaitTimeout()).isEqualTo(Duration.ofMillis(900));
        assertThat(driver.manage().timeouts().getPageLoadTimeout()).isEqualTo(Duration.ofSeconds(12));
        assertThat(driver.manage().timeouts().getScriptTimeout()).isEqualTo(Duration.ofSeconds(7));

        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(150));
        long missingStart = System.nanoTime();
        assertThatThrownBy(() -> driver.findElement(By.id("never-there")))
                .isInstanceOf(NoSuchElementException.class);
        Duration failedWait = Duration.ofNanos(System.nanoTime() - missingStart);
        assertThat(failedWait.toMillis()).isGreaterThanOrEqualTo(100L);
    }

    static void assertBrowserLogs(WebDriver driver, Path htmlFile) {
        driver.get(htmlFile.toUri().toString());

        assertThat(driver.manage().logs().getAvailableLogTypes())
                .contains(LogType.BROWSER, LogType.PERFORMANCE);
        assertThat(driver.manage().logs().get(LogType.BROWSER).getAll())
                .extracting(LogEntry::getMessage)
                .anyMatch(message -> message.contains("playwright-browser-log"));
        assertThat(driver.manage().logs().get(LogType.PERFORMANCE).getAll()).isEmpty();
    }

    static void assertOpenShadowDomHandling(WebDriver driver, Path htmlFile) {
        driver.get(htmlFile.toUri().toString());

        org.openqa.selenium.WebElement host = driver.findElement(By.id("shadow-host"));
        org.openqa.selenium.SearchContext shadowRoot = host.getShadowRoot();
        org.openqa.selenium.WebElement nestedButton = shadowRoot.findElement(By.cssSelector("[data-testid='shadow-button']"));
        org.openqa.selenium.WebElement nestedText = shadowRoot.findElement(By.id("shadow-text"));

        assertThat(nestedText.getText()).isEqualTo("Inside Shadow Root");
        nestedButton.click();
        assertThat(shadowRoot.findElement(By.id("shadow-status")).getText()).isEqualTo("clicked");
        assertThat(shadowRoot.findElements(By.cssSelector(".shadow-item"))).hasSize(2);
    }
}
