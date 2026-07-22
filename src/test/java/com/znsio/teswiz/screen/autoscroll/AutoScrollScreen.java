package com.znsio.teswiz.screen.autoscroll;

import com.znsio.teswiz.entities.Direction;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.autoscroll.AutoScrollScreenAndroid;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AutoScrollScreen {

    private static final String SCREEN_NAME = AutoScrollScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static AutoScrollScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(AutoScrollScreen.class);
    }

    public abstract AutoScrollScreen goToDropdownWindow();

    public abstract AutoScrollScreen scrollInDynamicLayer(Direction direction);

    public abstract boolean isScrollSuccessful();
}
