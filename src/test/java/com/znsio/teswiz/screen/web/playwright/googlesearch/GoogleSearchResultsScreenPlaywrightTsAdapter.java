package com.znsio.teswiz.screen.web.playwright.googlesearch;

import java.util.List;

import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.googlesearch.GoogleSearchResultsScreen;

public class GoogleSearchResultsScreenPlaywrightTsAdapter extends GoogleSearchResultsScreen {
    private static final By SEARCH_RESULT_HEADINGS = By.cssSelector("a div[role='heading']");

    private final Driver driver;
    private final Visual visually;
    private final String screenName = GoogleSearchResultsScreenPlaywrightTsAdapter.class.getSimpleName();

    public GoogleSearchResultsScreenPlaywrightTsAdapter(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public List<String> getSearchResults() {
        visually.checkWindow(screenName, "india - Google Search");
        return driver.findElements(SEARCH_RESULT_HEADINGS).stream()
                .map(webElement -> webElement.getText())
                .toList();
    }
}
