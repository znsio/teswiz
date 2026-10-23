package com.znsio.teswiz.screen.web.playwrightjava.googlesearch;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.googlesearch.GoogleSearchLandingScreen;
import com.znsio.teswiz.screen.googlesearch.GoogleSearchResultsScreen;

public class GoogleSearchLandingScreenPlaywrightJava extends GoogleSearchLandingScreen {
    private static final String URL = "https://google.com";
    private static final String SCREEN_NAME = GoogleSearchLandingScreenPlaywrightJava.class.getSimpleName();
    private static final By SEARCH_INPUT = By.cssSelector("textarea[name='q'], input[name='q']");

    private final Driver driver;
    private final Visual visually;

    public GoogleSearchLandingScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
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
