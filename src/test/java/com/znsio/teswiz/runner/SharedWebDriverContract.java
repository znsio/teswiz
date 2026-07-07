package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;

final class SharedWebDriverContract {
    private SharedWebDriverContract() {
    }

    static void assertNavigationPageSourceAndScreenshot(WebDriver driver, java.nio.file.Path htmlFile) {
        driver.get(htmlFile.toUri().toString());

        assertThat(driver.getCurrentUrl()).contains(htmlFile.getFileName().toString());
        assertThat(driver.getPageSource()).contains("Playwright Bridge");
        byte[] screenshot = ((org.openqa.selenium.TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        assertThat(screenshot).isNotEmpty();
    }

    static void assertCoreInteractions(WebDriver driver, java.nio.file.Path htmlFile) {
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

    static void assertJavascriptExecution(WebDriver driver, java.nio.file.Path htmlFile) {
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
}
