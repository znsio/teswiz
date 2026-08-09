package com.znsio.teswiz.screen.web.playwrightjava.theapp;

import com.microsoft.playwright.Locator;

import com.znsio.teswiz.screen.theapp.LoginScreen;
import com.znsio.teswiz.web.playwright.screen.PlaywrightJavaScreenContext;

public class LoginScreenPlaywrightJava extends LoginScreen {
    private static final String USER_NAME = "#username";
    private static final String PASSWORD = "#password";
    private static final String LOGIN_BUTTON = "button:has-text(\"Login\")";
    private static final String ERROR_MESSAGE = "#flash";
    private final com.znsio.teswiz.runner.Visual visually;
    private final Locator userName;
    private final Locator password;
    private final Locator loginButton;
    private final Locator errorMessage;
    private final String screenName = LoginScreenPlaywrightJava.class.getSimpleName();

    public LoginScreenPlaywrightJava(PlaywrightJavaScreenContext context) {
        this.visually = context.visual();
        this.userName = context.page().locator(USER_NAME);
        this.password = context.page().locator(PASSWORD);
        this.loginButton = context.page().locator(LOGIN_BUTTON);
        this.errorMessage = context.page().locator(ERROR_MESSAGE);
    }

    @Override
    public LoginScreen enterLoginDetails(String username, String password) {
        userName.fill(username);
        this.password.fill(password);
        visually.checkWindow(screenName, "Entered login details");
        return this;
    }

    @Override
    public LoginScreen login() {
        loginButton.click();
        visually.checkWindow(screenName, "Clicked on Login");
        return this;
    }

    @Override
    public String getInvalidLoginError() {
        visually.checkWindow(screenName, "Invalid Login alert");
        String alertText = errorMessage.textContent();
        return null == alertText ? "" : alertText.trim();
    }

    @Override
    public LoginScreen dismissAlert() {
        visually.checkWindow(screenName, "Invalid Login alert dismissed");
        return this;
    }
}
