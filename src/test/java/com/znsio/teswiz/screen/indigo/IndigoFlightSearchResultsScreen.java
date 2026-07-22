package com.znsio.teswiz.screen.indigo;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.indigo.IndigoFlightSearchResultsScreenAndroid;
import com.znsio.teswiz.screen.web.indigo.IndigoFlightSearchResultsScreenWeb;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class IndigoFlightSearchResultsScreen {
    private static final String SCREEN_NAME = IndigoFlightSearchResultsScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static IndigoFlightSearchResultsScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(IndigoFlightSearchResultsScreen.class);
    }
}
