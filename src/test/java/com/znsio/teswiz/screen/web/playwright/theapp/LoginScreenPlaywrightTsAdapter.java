package com.znsio.teswiz.screen.web.playwright.theapp;

import static com.znsio.teswiz.tools.Wait.waitFor;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.theapp.LoginScreen;
import com.znsio.teswiz.web.playwright.PlaywrightTsScreenActionExecutor;

public class LoginScreenPlaywrightTsAdapter extends LoginScreen {
    private static final String SCREEN_MODULE = "theapp/login.screen.ts";
    private final Driver driver;
    private final Visual visually;
    private final String screenName = LoginScreenPlaywrightTsAdapter.class.getSimpleName();
    private final PlaywrightTsScreenActionExecutor screenActionExecutor;

    public LoginScreenPlaywrightTsAdapter(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        this.screenActionExecutor = new PlaywrightTsScreenActionExecutor(driver);
    }

    @Override
    public LoginScreen enterLoginDetails(String username, String password) {
        waitFor(1);
        screenActionExecutor.run(SCREEN_MODULE, "enterLoginDetails", username, password);
        visually.checkWindow(screenName, "Entered login details");
        return this;
    }

    @Override
    public LoginScreen login() {
        screenActionExecutor.run(SCREEN_MODULE, "login");
        waitFor(1);
        visually.checkWindow(screenName, "Clicked on Login");
        return this;
    }

    @Override
    public String getInvalidLoginError() {
        visually.checkWindow(screenName, "Invalid Login alert");
        return screenActionExecutor.runForString(SCREEN_MODULE, "getInvalidLoginError").trim();
    }

    @Override
    public LoginScreen dismissAlert() {
        screenActionExecutor.run(SCREEN_MODULE, "dismissAlert");
        visually.checkWindow(screenName, "Invalid Login alert dismissed");
        return this;
    }
}
