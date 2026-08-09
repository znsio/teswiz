package com.znsio.teswiz.screen.web.playwrightjava.theapp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.playwright.Locator;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.theapp.AppLaunchScreen;
import com.znsio.teswiz.screen.theapp.ClipboardDemoScreen;
import com.znsio.teswiz.screen.theapp.EchoScreen;
import com.znsio.teswiz.screen.theapp.LoginScreen;
import com.znsio.teswiz.web.playwright.screen.PlaywrightJavaScreenContext;

public class AppLaunchScreenPlaywrightJava extends AppLaunchScreen {
    private static final Logger LOGGER = LogManager.getLogger(AppLaunchScreenPlaywrightJava.class.getName());
    private static final String LOGIN_FORM_LINK = "a:has-text(\"Form Authentication\")";
    private static final String ENGINE_NAME = "playwright-java";
    private final Driver driver;
    private final Visual visually;
    private final Locator loginFormLink;
    private final String screenName = AppLaunchScreenPlaywrightJava.class.getSimpleName();

    public AppLaunchScreenPlaywrightJava(PlaywrightJavaScreenContext context) {
        this.driver = context.driver();
        this.visually = context.visual();
        this.loginFormLink = context.page().locator(LOGIN_FORM_LINK);
        visually.checkWindow(screenName, "Home screen");
    }

    @Override
    public LoginScreen selectLogin() {
        driver.printAndSavePageSourceDump();
        loginFormLink.click();
        return LoginScreen.get();
    }

    @Override
    public AppLaunchScreen goBack() {
        LOGGER.info("Skipping this step for Playwright Java web");
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
