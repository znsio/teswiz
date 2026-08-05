package com.znsio.teswiz.screen.web.playwrightjava.theapp;

import static com.znsio.teswiz.tools.Wait.waitFor;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.theapp.LoginScreen;

public class LoginScreenPlaywrightJava extends LoginScreen {
    private static final By USER_NAME = By.id("username");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BUTTON = By.xpath("//button/i[contains(text(),\"Login\")]");
    private static final By ERROR_MESSAGE = By.id("flash");
    private final Driver driver;
    private final Visual visually;
    private final String screenName = LoginScreenPlaywrightJava.class.getSimpleName();

    public LoginScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public LoginScreen enterLoginDetails(String username, String password) {
        waitFor(2);
        driver.findElement(USER_NAME).sendKeys(username);
        driver.findElement(PASSWORD).sendKeys(password);
        visually.checkWindow(screenName, "Entered login details");
        return this;
    }

    @Override
    public LoginScreen login() {
        driver.findElement(LOGIN_BUTTON).click();
        waitFor(2);
        visually.checkWindow(screenName, "Clicked on Login");
        return this;
    }

    @Override
    public String getInvalidLoginError() {
        WebElement alertText = driver.waitForClickabilityOf(ERROR_MESSAGE);
        visually.checkWindow(screenName, "Invalid Login alert");
        return alertText.getText().trim();
    }

    @Override
    public LoginScreen dismissAlert() {
        waitFor(2);
        visually.checkWindow(screenName, "Invalid Login alert dismissed");
        return this;
    }
}
