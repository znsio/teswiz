package com.znsio.teswiz.screen.theapp;


public abstract class LoginScreen {

    public static LoginScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(LoginScreen.class);
    }

    public abstract LoginScreen enterLoginDetails(String username, String password);

    public abstract LoginScreen login();

    public abstract String getInvalidLoginError();

    public abstract LoginScreen dismissAlert();
}
