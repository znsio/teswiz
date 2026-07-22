package com.znsio.teswiz.screen.helloWorld;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ios.helloWorld.HelloWorldScreenIOS;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class HelloWorldScreen {
    private static final String SCREEN_NAME = HelloWorldScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static HelloWorldScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(HelloWorldScreen.class);
    }

    public abstract HelloWorldScreen generateRandomNumber(int counter);
}
