package com.znsio.e2e.screen.web;

import com.znsio.e2e.screen.HomeScreen;
import com.znsio.e2e.screen.LoginScreen;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import org.openqa.selenium.By;

public class HomeScreenWeb extends HomeScreen {
    private final Driver driver;
    private final Visual visually;
    private final String screenName = HomeScreenWeb.class.getSimpleName();
    private By loginFormLinkText = By.linkText("Form Authentication");

    public HomeScreenWeb (Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        visually.takeScreenshot(screenName, "Home screen");
    }

    @Override
    public LoginScreen selectLoginTest () {
        driver.findElement(loginFormLinkText).click();
        return LoginScreen.get();
    }
}
