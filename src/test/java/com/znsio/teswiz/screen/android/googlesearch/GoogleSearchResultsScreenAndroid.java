package com.znsio.teswiz.screen.android.googlesearch;

import com.context.TestExecutionContext;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.googlesearch.GoogleSearchResultsScreen;
import com.znsio.teswiz.screen.web.dineout.DineoutLandingScreenWeb;
import org.apache.log4j.Logger;

public class GoogleSearchResultsScreenAndroid extends GoogleSearchResultsScreen {
    private static final String SCREEN_NAME = DineoutLandingScreenWeb.class.getSimpleName();

    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private static final String NOT_YET_IMPLEMENTED = " not yet implemented";

    private final Driver driver;
    private final Visual visually;
    private final TestExecutionContext context;

    public GoogleSearchResultsScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        long threadId = Thread.currentThread().getId();
        context = Runner.getTestExecutionContext(threadId);
    }

}
