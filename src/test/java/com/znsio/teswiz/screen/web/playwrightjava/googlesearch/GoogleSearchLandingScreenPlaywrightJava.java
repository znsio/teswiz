package com.znsio.teswiz.screen.web.playwrightjava.googlesearch;

import com.microsoft.playwright.Locator;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.googlesearch.GoogleSearchLandingScreen;
import com.znsio.teswiz.screen.googlesearch.GoogleSearchResultsScreen;
import com.znsio.teswiz.web.playwright.screen.PlaywrightJavaScreenContext;

public class GoogleSearchLandingScreenPlaywrightJava extends GoogleSearchLandingScreen {
    private static final String URL = "https://google.com";
    private static final String SCREEN_NAME = GoogleSearchLandingScreenPlaywrightJava.class.getSimpleName();
    private static final String SEARCH_INPUT = "textarea[name='q'], input[name='q']";

    private final PlaywrightJavaScreenContext context;
    private final Visual visually;

    public GoogleSearchLandingScreenPlaywrightJava(PlaywrightJavaScreenContext context) {
        this.context = context;
        this.visually = context.visual();
        context.page().navigate(URL);
        context.page().locator(SEARCH_INPUT).waitFor();
    }

    @Override
    public GoogleSearchResultsScreen searchFor(String searchText) {
        visually.checkWindow(SCREEN_NAME, "Google");
        Locator input = context.page().locator(SEARCH_INPUT);
        input.fill(searchText);
        input.press("Enter");
        return GoogleSearchResultsScreen.get();
    }
}
