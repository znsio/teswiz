package com.znsio.teswiz.screen.theapp;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.theapp.LoginScreenAndroid;
import com.znsio.teswiz.screen.ios.theapp.LoginScreenIOS;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class LoginScreen {
    private static final String SCREEN_NAME = LoginScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static LoginScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(LoginScreen.class);
    }

    public abstract LoginScreen enterLoginDetails(String username, String password);

    public abstract LoginScreen login();

    public abstract String getInvalidLoginError();

    public abstract LoginScreen dismissAlert();
}
