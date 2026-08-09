package com.znsio.teswiz.screen.web.playwrightjava.googlesearch;

import java.util.List;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.googlesearch.GoogleSearchResultsScreen;
import com.znsio.teswiz.web.playwright.screen.PlaywrightJavaScreenContext;

public class GoogleSearchResultsScreenPlaywrightJava extends GoogleSearchResultsScreen {
    private static final String SCREEN_NAME = GoogleSearchResultsScreenPlaywrightJava.class.getSimpleName();
    private static final String SEARCH_RESULTS_HEADINGS = "a div[role='heading']";

    private final PlaywrightJavaScreenContext context;
    private final Visual visually;

    public GoogleSearchResultsScreenPlaywrightJava(PlaywrightJavaScreenContext context) {
        this.context = context;
        this.visually = context.visual();
    }

    @Override
    public List<String> getSearchResults() {
        visually.checkWindow(SCREEN_NAME, "india - Google Search");
        return context.page().locator(SEARCH_RESULTS_HEADINGS).allInnerTexts();
    }
}
