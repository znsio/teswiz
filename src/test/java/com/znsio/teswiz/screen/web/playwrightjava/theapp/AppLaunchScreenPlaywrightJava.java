package com.znsio.teswiz.screen.web.playwrightjava.theapp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.theapp.AppLaunchScreen;
import com.znsio.teswiz.screen.theapp.ClipboardDemoScreen;
import com.znsio.teswiz.screen.theapp.EchoScreen;
import com.znsio.teswiz.screen.theapp.LoginScreen;

public class AppLaunchScreenPlaywrightJava extends AppLaunchScreen {
    private static final Logger LOGGER = LogManager.getLogger(AppLaunchScreenPlaywrightJava.class.getName());
    private static final By LOGIN_FORM_LINK = By.linkText("Form Authentication");
    private final Driver driver;
    private final Visual visually;
    private final String screenName = AppLaunchScreenPlaywrightJava.class.getSimpleName();

    public AppLaunchScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        visually.checkWindow(screenName, "Home screen");
    }

    @Override
    public LoginScreen selectLogin() {
        driver.printAndSavePageSourceDump();
        driver.findElement(LOGIN_FORM_LINK).click();
        return LoginScreen.get();
    }

    @Override
    public AppLaunchScreen goBack() {
        LOGGER.info("Skipping this step for Playwright Java web");
        return this;
    }

    @Override
    public EchoScreen selectEcho() {
        LOGGER.info("Skipping this step for Playwright Java web");
        return EchoScreen.get();
    }

    @Override
    public ClipboardDemoScreen goToClipboardDemo() {
        return null;
    }
}
