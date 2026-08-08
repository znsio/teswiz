package com.znsio.teswiz.screen.googlesearch;


public abstract class GoogleSearchLandingScreen {

    public static GoogleSearchLandingScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(GoogleSearchLandingScreen.class);
    }

    public abstract GoogleSearchResultsScreen searchFor(String searchText);

}
