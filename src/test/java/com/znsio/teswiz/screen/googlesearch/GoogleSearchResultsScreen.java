package com.znsio.teswiz.screen.googlesearch;


import java.util.List;

public abstract class GoogleSearchResultsScreen {

    public static GoogleSearchResultsScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(GoogleSearchResultsScreen.class);
    }

    public abstract List<String> getSearchResults();
}
