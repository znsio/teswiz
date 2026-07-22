package com.znsio.teswiz.screen.theapp;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.theapp.AppLaunchScreenAndroid;
import com.znsio.teswiz.screen.ios.theapp.AppLaunchScreenIOS;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AppLaunchScreen {
    private static final String SCREEN_NAME = AppLaunchScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static AppLaunchScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(AppLaunchScreen.class);
    }

    public abstract LoginScreen selectLogin();

    public abstract AppLaunchScreen goBack();

    public abstract EchoScreen selectEcho();

    public abstract ClipboardDemoScreen goToClipboardDemo();
}
