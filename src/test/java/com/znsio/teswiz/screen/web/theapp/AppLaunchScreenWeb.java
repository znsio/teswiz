package com.znsio.teswiz.screen.web.theapp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.theapp.AppLaunchScreen;
import com.znsio.teswiz.screen.theapp.ClipboardDemoScreen;
import com.znsio.teswiz.screen.theapp.EchoScreen;
import com.znsio.teswiz.screen.theapp.LoginScreen;

public class AppLaunchScreenWeb
        extends AppLaunchScreen {
    private static final Logger LOGGER = LogManager.getLogger(AppLaunchScreenWeb.class.getName());
    private static final By loginFormLinkText = By.linkText("Form Authentication");
    private static final String ENGINE_NAME = "selenium";
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = AppLaunchScreenWeb.class.getSimpleName();

    public AppLaunchScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        visually.checkWindow(SCREEN_NAME, "Home screen");
    }

    @Override
    public LoginScreen selectLogin() {
        driver.printAndSavePageSourceDump();
        driver.findElement(loginFormLinkText).click();
        return LoginScreen.get();
    }

    @Override
    public AppLaunchScreen goBack() {
        LOGGER.info("Skipping this step for Web");
        return this;
    }

    @Override
    public EchoScreen selectEcho() {
        throw unsupported("Echo");
    }

    @Override
    public ClipboardDemoScreen goToClipboardDemo() {
        throw unsupported("Clipboard Demo");
    }

    private UnsupportedOperationException unsupported(String screenName) {
        return new UnsupportedOperationException(String.format(
                "TheApp %s screen is not supported on web for WEB_ENGINE=%s.",
                screenName, ENGINE_NAME));
    }
}
