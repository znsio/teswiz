package com.znsio.e2e.screen.web;

import com.znsio.e2e.screen.EchoScreen;
import com.znsio.e2e.screen.HomeScreen;
import com.znsio.e2e.screen.LoginScreen;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.By;

public class HomeScreenWeb extends HomeScreen {
    private static final String NOT_YET_IMPLEMENTED = "NOT_YET_IMPLEMENTED";
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = HomeScreenWeb.class.getSimpleName();
    private By loginFormLinkText = By.linkText("Form Authentication");

    public HomeScreenWeb (Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        visually.takeScreenshot(SCREEN_NAME, "Home screen");
    }

    @Override
    public LoginScreen selectLogin () {
        driver.findElement(loginFormLinkText).click();
        return LoginScreen.get();
    }

    @Override
    public HomeScreen goBack () {
        throw new NotImplementedException(SCREEN_NAME + ":"
                + new Throwable().getStackTrace()[0].getMethodName()
                + NOT_YET_IMPLEMENTED);
    }

    @Override
    public EchoScreen selectEcho () {
        throw new NotImplementedException(SCREEN_NAME + ":"
                + new Throwable().getStackTrace()[0].getMethodName()
                + NOT_YET_IMPLEMENTED);
    }
}
