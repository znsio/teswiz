package com.znsio.teswiz.screen.web.playwright.googlesearch;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.googlesearch.GoogleSearchLandingScreen;
import com.znsio.teswiz.screen.googlesearch.GoogleSearchResultsScreen;

public class GoogleSearchLandingScreenPlaywrightTsAdapter extends GoogleSearchLandingScreen {
    private static final String URL = "https://google.com";
    private static final By SEARCH_INPUT = By.name("q");

    private final Driver driver;
    private final Visual visually;
    private final String screenName = GoogleSearchLandingScreenPlaywrightTsAdapter.class.getSimpleName();

    public GoogleSearchLandingScreenPlaywrightTsAdapter(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        openLandingPage();
    }

    @Override
    public GoogleSearchResultsScreen searchFor(String searchText) {
        visually.checkWindow(screenName, "Google");
        driver.findElement(SEARCH_INPUT).sendKeys(searchText, Keys.ENTER);
        return GoogleSearchResultsScreen.get();
    }

    private void openLandingPage() {
        driver.getInnerDriver().get(URL);
        driver.waitTillElementIsPresent(SEARCH_INPUT);
    }
}
