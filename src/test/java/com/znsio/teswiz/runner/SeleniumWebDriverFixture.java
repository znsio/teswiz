package com.znsio.teswiz.runner;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.opentest4j.TestAbortedException;

final class SeleniumWebDriverFixture implements SharedWebDriverFixture {
    private final WebDriver webDriver;

    SeleniumWebDriverFixture() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1440,1024");
        SeleniumContractConditions.resolveChromeBinary().ifPresent(options::setBinary);

        try {
            webDriver = new ChromeDriver(options);
            webDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));
        } catch (RuntimeException exception) {
            throw new TestAbortedException(String.format(
                    "Unable to start Selenium Chrome contract fixture. Enable with -D%s=true and ensure Chrome/driver provisioning is available. Root cause: %s",
                    SeleniumContractConditions.ENABLE_PROPERTY,
                    exception.getMessage()), exception);
        }
    }

    @Override
    public WebDriver createDriver(String userPersona) {
        return webDriver;
    }

    @Override
    public void close() {
        webDriver.quit();
    }
}
