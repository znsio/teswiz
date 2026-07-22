package com.znsio.teswiz.screen.web.playwright.theapp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.theapp.AppLaunchScreen;
import com.znsio.teswiz.screen.theapp.ClipboardDemoScreen;
import com.znsio.teswiz.screen.theapp.EchoScreen;
import com.znsio.teswiz.screen.theapp.LoginScreen;
import com.znsio.teswiz.web.playwright.PlaywrightBy;

public class AppLaunchScreenPlaywrightTs extends AppLaunchScreen {
    private static final Logger LOGGER = LogManager.getLogger(AppLaunchScreenPlaywrightTs.class.getName());
    private static final By LOGIN_LINK = PlaywrightBy.role("link", "Form Authentication");
    private final Driver driver;
    private final Visual visually;
    private final String screenName = AppLaunchScreenPlaywrightTs.class.getSimpleName();

    public AppLaunchScreenPlaywrightTs(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        visually.checkWindow(screenName, "Home screen");
    }

    @Override
    public LoginScreen selectLogin() {
        driver.findElement(LOGIN_LINK).click();
        return LoginScreen.get();
    }

    @Override
    public AppLaunchScreen goBack() {
        LOGGER.info("Skipping this step for Web");
        return this;
    }

    @Override
    public EchoScreen selectEcho() {
        LOGGER.info("Skipping this step for Web");
        return EchoScreen.get();
    }

    @Override
    public ClipboardDemoScreen goToClipboardDemo() {
        return null;
    }
}
