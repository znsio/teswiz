package com.znsio.teswiz.screen.android.theapp;

import com.applitools.eyes.appium.Target;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.theapp.LoginScreen;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.znsio.teswiz.tools.Wait.waitFor;

public class LoginScreenAndroid
        extends LoginScreen {
    private static final By errorMessageId = By.id("android:id/message");
    private static final By dismissAlertId = By.id("android:id/button1");
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = LoginScreenAndroid.class.getSimpleName();
    private final String userNameId = "username";
    private final String passwordId = "password";
    private final String loginButtonId = "loginBtn";

    public LoginScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public LoginScreen enterLoginDetails(String username, String password) {
        waitFor(2);
        WebElement userNameElement = driver.findElementByAccessibilityId(userNameId);
        userNameElement.clear();
        userNameElement.sendKeys(username);
        WebElement passwordElement = driver.findElementByAccessibilityId(passwordId);
        passwordElement.clear();
        passwordElement.sendKeys(password);
        //        driver.waitForVisibilityOf(passwordId).sendKeys(username);
        visually.check(SCREEN_NAME, "Entered login details",
                       Target.window().fully().layout(userNameElement, passwordElement));
        return this;
    }

    @Override
    public LoginScreen login() {
        waitFor(1);
        driver.findElementByAccessibilityId(loginButtonId).click();
        waitFor(2);
        visually.checkWindow(SCREEN_NAME, "Clicked on Login");
        return this;
    }

    @Override
    public String getInvalidLoginError() {
        WebElement alertText = driver.waitForClickabilityOf(errorMessageId);
        visually.checkWindow(SCREEN_NAME, "Invalid Login alert");
        return alertText.getText();
    }

    @Override
    public LoginScreen dismissAlert() {
        driver.waitForClickabilityOf(dismissAlertId).click();
        waitFor(2);
        visually.checkWindow(SCREEN_NAME, "Invalid Login alert dismissed");
        return this;
    }
}
