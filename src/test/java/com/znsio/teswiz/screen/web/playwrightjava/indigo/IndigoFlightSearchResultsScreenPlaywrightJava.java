package com.znsio.teswiz.screen.web.playwrightjava.indigo;

import com.microsoft.playwright.Locator;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.indigo.IndigoFlightSearchResultsScreen;
import com.znsio.teswiz.web.playwright.screen.PlaywrightJavaScreenContext;

public class IndigoFlightSearchResultsScreenPlaywrightJava extends IndigoFlightSearchResultsScreen {
    private static final String SCREEN_NAME = IndigoFlightSearchResultsScreenPlaywrightJava.class.getSimpleName();
    private static final String BACK_TO_SEARCH_RESULTS = "div.bck-to-search";

    public IndigoFlightSearchResultsScreenPlaywrightJava(PlaywrightJavaScreenContext context) {
        context.page().locator(BACK_TO_SEARCH_RESULTS).waitFor(new Locator.WaitForOptions().setTimeout(30000));
        context.visual().checkWindow(SCREEN_NAME, "On Search Results page");
    }
}
