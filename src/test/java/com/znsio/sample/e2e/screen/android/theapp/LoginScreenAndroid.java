package com.znsio.sample.e2e.screen.android.theapp;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.theapp.LoginScreen;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.znsio.e2e.tools.Wait.waitFor;

public class LoginScreenAndroid
        extends LoginScreen {
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = LoginScreenAndroid.class.getSimpleName();

    private final String userNameId = "username";
    private final String passwordId = "password";
    private final By loginButtonXpath = By.xpath("//android.widget.Button[@content-desc=\"loginBtn\"]/android.widget.TextView");
    private final By errorMessageId = By.id("android:id/message");
    private final By dismissAlertId = By.id("android:id/button1");

    public LoginScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public LoginScreen enterLoginDetails(String username, String password) {
        waitFor(2);
        driver.findElementByAccessibilityId(userNameId)
              .clear();
        driver.findElementByAccessibilityId(userNameId)
              .sendKeys(username);
        driver.findElementByAccessibilityId(passwordId)
              .clear();
        driver.findElementByAccessibilityId(passwordId)
              .sendKeys(password);
        //        driver.waitForVisibilityOf(passwordId).sendKeys(username);
        visually.takeScreenshot(SCREEN_NAME, "enterLoginDetails");
        visually.checkWindow(SCREEN_NAME, "entered login details");
        return this;
    }

    @Override
    public LoginScreen login() {
        driver.findElement(loginButtonXpath)
              .click();
        waitFor(2);
        return this;
    }

    @Override
    public String getInvalidLoginError() {
        WebElement alertText = driver.waitForClickabilityOf(errorMessageId);
        visually.takeScreenshot(SCREEN_NAME, "Invalid Login alert");
        visually.checkWindow(SCREEN_NAME, "Invalid Login alert");
        return alertText.getText();
    }

    @Override
    public LoginScreen dismissAlert() {
        driver.waitForClickabilityOf(dismissAlertId)
              .click();
        waitFor(2);
        visually.takeScreenshot(SCREEN_NAME, "Invalid Login alert dismissed");
        return this;
    }
}
