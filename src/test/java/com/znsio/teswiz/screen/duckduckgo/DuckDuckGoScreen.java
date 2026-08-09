package com.znsio.teswiz.screen.duckduckgo;


public abstract class DuckDuckGoScreen {

    public static DuckDuckGoScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(DuckDuckGoScreen.class);
    }

    public abstract DuckDuckGoScreen launchBrowser();

    public abstract DuckDuckGoScreen cancelChangingDefaultBrowserPopup();

    public abstract String getDefaultTextFromWebView();

    public abstract DuckDuckGoScreen switchToNativeContextAndGoToTeswizGithub();
}
