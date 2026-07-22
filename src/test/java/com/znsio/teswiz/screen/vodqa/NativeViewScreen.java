package com.znsio.teswiz.screen.vodqa;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.vodqa.NativeViewScreenAndroid;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class NativeViewScreen {
    private static final String SCREEN_NAME = NativeViewScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static NativeViewScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(NativeViewScreen.class);
    }

    public abstract boolean isUserOnNativeViewScreen();
}
