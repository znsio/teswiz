package com.znsio.teswiz.screen.duckduckgo;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.duckduckgo.DuckDuckGoScreenAndroid;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DuckDuckGoScreen {
    private static final String SCREEN_NAME = DuckDuckGoScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static DuckDuckGoScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(DuckDuckGoScreen.class);
    }

    public abstract DuckDuckGoScreen launchBrowser();

    public abstract DuckDuckGoScreen cancelChangingDefaultBrowserPopup();

    public abstract String getDefaultTextFromWebView();

    public abstract DuckDuckGoScreen switchToNativeContextAndGoToTeswizGithub();
}
