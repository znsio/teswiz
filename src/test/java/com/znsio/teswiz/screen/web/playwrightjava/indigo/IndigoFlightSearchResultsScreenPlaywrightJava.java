package com.znsio.teswiz.screen.web.playwrightjava.indigo;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.indigo.IndigoFlightSearchResultsScreen;
import org.openqa.selenium.By;

public class IndigoFlightSearchResultsScreenPlaywrightJava extends IndigoFlightSearchResultsScreen {
    private static final String SCREEN_NAME = IndigoFlightSearchResultsScreenPlaywrightJava.class.getSimpleName();
    private static final By BACK_TO_SEARCH_RESULTS = By.xpath("//div[@class='bck-to-search']");

    public IndigoFlightSearchResultsScreenPlaywrightJava(Driver driver, Visual visually) {
        driver.waitTillElementIsVisible(BACK_TO_SEARCH_RESULTS, 30);
        visually.checkWindow(SCREEN_NAME, "On Search Results page");
    }
}
