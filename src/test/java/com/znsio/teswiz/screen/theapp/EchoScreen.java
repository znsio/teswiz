package com.znsio.teswiz.screen.theapp;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.theapp.EchoScreenAndroid;
import com.znsio.teswiz.screen.web.theapp.EchoScreenWeb;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class EchoScreen {
    private static final String SCREEN_NAME = EchoScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static EchoScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(EchoScreen.class);
    }

    public abstract EchoScreen echoMessage(String message);
}
