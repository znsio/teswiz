package com.znsio.teswiz.screen.web.playwright.theapp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.theapp.AppLaunchScreen;
import com.znsio.teswiz.screen.theapp.ClipboardDemoScreen;
import com.znsio.teswiz.screen.theapp.EchoScreen;
import com.znsio.teswiz.screen.theapp.LoginScreen;
import com.znsio.teswiz.web.playwright.PlaywrightTsScreenActionExecutor;

public class AppLaunchScreenPlaywrightTs extends AppLaunchScreen {
    private static final Logger LOGGER = LogManager.getLogger(AppLaunchScreenPlaywrightTs.class.getName());
    private static final String SCREEN_MODULE = "theapp/app-launch.screen.ts";
    private final Driver driver;
    private final Visual visually;
    private final String screenName = AppLaunchScreenPlaywrightTs.class.getSimpleName();
    private final PlaywrightTsScreenActionExecutor screenActionExecutor;

    public AppLaunchScreenPlaywrightTs(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        this.screenActionExecutor = new PlaywrightTsScreenActionExecutor(driver);
        visually.checkWindow(screenName, "Home screen");
    }

    @Override
    public LoginScreen selectLogin() {
        screenActionExecutor.run(SCREEN_MODULE, "selectLogin");
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
