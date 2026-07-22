package com.znsio.teswiz.screen;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.ScreenShotScreenAndroid;
import com.znsio.teswiz.screen.web.ScreenShotScreenWeb;
import com.znsio.teswiz.screen.windows.ScreenShotScreenWindows;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ScreenShotScreen {
    private static final String SCREEN_NAME = ScreenShotScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static ScreenShotScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(ScreenShotScreen.class);
    }

    public abstract ScreenShotScreen takeScreenshot();
}
