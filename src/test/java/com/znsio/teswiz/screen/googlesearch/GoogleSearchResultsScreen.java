package com.znsio.teswiz.screen.googlesearch;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.googlesearch.GoogleSearchResultsScreenAndroid;
import com.znsio.teswiz.screen.web.googlesearch.GoogleSearchResultsScreenWeb;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public abstract class GoogleSearchResultsScreen {
    private static final String SCREEN_NAME = GoogleSearchResultsScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static GoogleSearchResultsScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(GoogleSearchResultsScreen.class);
    }

    public abstract List<String> getSearchResults();
}
