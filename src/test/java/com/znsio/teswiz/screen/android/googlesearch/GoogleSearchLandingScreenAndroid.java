package com.znsio.teswiz.screen.android.googlesearch;

import com.context.TestExecutionContext;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.googlesearch.GoogleSearchLandingScreen;
import com.znsio.teswiz.screen.googlesearch.GoogleSearchResultsScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

public class GoogleSearchLandingScreenAndroid extends GoogleSearchLandingScreen {
    private static final String URL = "https://google.com";
    private static final String SCREEN_NAME = GoogleSearchLandingScreenAndroid.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private static final By SEARCH_INPUT = By.name("q");

    private final Driver driver;
    private final Visual visually;
    private final TestExecutionContext context;

    public GoogleSearchLandingScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        long threadId = Thread.currentThread().getId();
        context = Runner.getTestExecutionContext(threadId);
        driver.getInnerDriver().get(URL);
        driver.waitTillElementIsPresent(SEARCH_INPUT);
    }

    @Override
    public GoogleSearchResultsScreen searchFor(String searchText) {
        visually.checkWindow(SCREEN_NAME, "Google");
        driver.findElement(SEARCH_INPUT).sendKeys(searchText, Keys.ENTER);
        return GoogleSearchResultsScreen.get();
    }
}