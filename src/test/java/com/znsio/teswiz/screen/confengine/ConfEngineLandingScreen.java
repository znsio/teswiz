package com.znsio.teswiz.screen.confengine;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.confengine.ConfEngineLandingScreenAndroid;
import com.znsio.teswiz.screen.web.confengine.ConfEngineLandingScreenWeb;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ConfEngineLandingScreen {
    private static final String SCREEN_NAME = ConfEngineLandingScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static ConfEngineLandingScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(ConfEngineLandingScreen.class);
    }

    public abstract ConfEngineLandingScreen getListOfConferences();
}
