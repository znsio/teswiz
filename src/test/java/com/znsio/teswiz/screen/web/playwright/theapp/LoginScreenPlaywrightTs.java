package com.znsio.teswiz.screen.web.playwright.theapp;

import static com.znsio.teswiz.tools.Wait.waitFor;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.theapp.LoginScreen;
import com.znsio.teswiz.web.playwright.PlaywrightBy;

public class LoginScreenPlaywrightTs extends LoginScreen {
    private static final By USERNAME_INPUT = By.id("username");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON = PlaywrightBy.role("button", "Login");
    private static final By ERROR_MESSAGE = By.id("flash");
    private final Driver driver;
    private final Visual visually;
    private final String screenName = LoginScreenPlaywrightTs.class.getSimpleName();

    public LoginScreenPlaywrightTs(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public LoginScreen enterLoginDetails(String username, String password) {
        waitFor(1);
        driver.findElement(USERNAME_INPUT).sendKeys(username);
        driver.findElement(PASSWORD_INPUT).sendKeys(password);
        visually.checkWindow(screenName, "Entered login details");
        return this;
    }

    @Override
    public LoginScreen login() {
        driver.findElement(LOGIN_BUTTON).click();
        waitFor(1);
        visually.checkWindow(screenName, "Clicked on Login");
        return this;
    }

    @Override
    public String getInvalidLoginError() {
        WebElement alertText = driver.waitTillElementIsPresent(ERROR_MESSAGE);
        visually.checkWindow(screenName, "Invalid Login alert");
        return alertText.getText().trim();
    }

    @Override
    public LoginScreen dismissAlert() {
        visually.checkWindow(screenName, "Invalid Login alert dismissed");
        return this;
    }
}
