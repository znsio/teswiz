package com.znsio.teswiz.screen.vodqa;


public abstract class WebViewScreen {

    public static WebViewScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(WebViewScreen.class);
    }

    public abstract boolean isUserOnNewsWebViewScreen();

    public abstract boolean isLoginOptionVisible();

    public abstract VodqaScreen navigateToSamplesList();
}
