package com.znsio.teswiz.screen.web.playwrightjava.googlesearch;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.googlesearch.GoogleSearchResultsScreen;

public class GoogleSearchResultsScreenPlaywrightJava extends GoogleSearchResultsScreen {
    private static final String SCREEN_NAME = GoogleSearchResultsScreenPlaywrightJava.class.getSimpleName();
    private static final By SEARCH_RESULTS_HEADINGS = By.cssSelector("a div[role='heading']");

    private final Driver driver;
    private final Visual visually;

    public GoogleSearchResultsScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public List<String> getSearchResults() {
        visually.checkWindow(SCREEN_NAME, "india - Google Search");
        return driver.findElements(SEARCH_RESULTS_HEADINGS).stream()
                .map(webElement -> webElement.getText())
                .collect(Collectors.toList());
    }
}
