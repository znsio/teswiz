package com.znsio.teswiz.screen.theapp;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.theapp.ClipboardDemoScreenAndroid;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ClipboardDemoScreen {
    private static final String SCREEN_NAME = ClipboardDemoScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static ClipboardDemoScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(ClipboardDemoScreen.class);
    }

    public abstract ClipboardDemoScreen setInClipboard(String content);

    public abstract boolean doesAddedContentExistInClipboard();
}
