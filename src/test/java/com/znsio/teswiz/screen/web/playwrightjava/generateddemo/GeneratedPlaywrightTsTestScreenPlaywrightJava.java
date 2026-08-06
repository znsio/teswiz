package com.znsio.teswiz.screen.web.playwrightjava.generateddemo;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.generateddemo.GeneratedPlaywrightTsTestScreen;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

import java.util.List;

public class GeneratedPlaywrightTsTestScreenPlaywrightJava extends GeneratedPlaywrightTsTestScreen {
    private static final By INPUT = By.id("demo-input");
    private static final By OUTPUT = By.id("demo-output");

    private final Driver driver;

    public GeneratedPlaywrightTsTestScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
    }

    @Override
    public GeneratedPlaywrightTsTestScreen open(String url) {
        driver.getInnerDriver().get(url);
        return this;
    }

    @Override
    public GeneratedPlaywrightTsTestScreen enterValue(String value) {
        driver.findElement(INPUT).clear();
        driver.findElement(INPUT).sendKeys(value);
        ((JavascriptExecutor) driver.getInnerDriver()).executeScript("arguments[0].textContent = arguments[1]",
                driver.findElement(OUTPUT), value);
        return this;
    }

    @Override
    public String readValue() {
        return driver.findElement(OUTPUT).getText().trim();
    }

    @Override
    public List<String> readValues() {
        String currentValue = readValue();
        return List.of(currentValue, currentValue + "-copy");
    }
}
