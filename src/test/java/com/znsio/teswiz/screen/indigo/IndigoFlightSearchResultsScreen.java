package com.znsio.teswiz.screen.indigo;


public abstract class IndigoFlightSearchResultsScreen {

    public static IndigoFlightSearchResultsScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(IndigoFlightSearchResultsScreen.class);
    }
}
