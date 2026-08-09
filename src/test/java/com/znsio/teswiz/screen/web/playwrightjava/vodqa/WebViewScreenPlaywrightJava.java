package com.znsio.teswiz.screen.web.playwrightjava.vodqa;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.vodqa.VodqaScreen;
import com.znsio.teswiz.screen.vodqa.WebViewScreen;

public class WebViewScreenPlaywrightJava extends WebViewScreen {
    private static final String ENGINE_NAME = "playwright-java";

    public WebViewScreenPlaywrightJava(Driver driver, Visual visually) {
    }

    @Override
    public boolean isUserOnNewsWebViewScreen() {
        throw unsupported("news web view");
    }

    @Override
    public boolean isLoginOptionVisible() {
        throw unsupported("login option");
    }

    @Override
    public VodqaScreen navigateToSamplesList() {
        throw unsupported("navigate to samples list");
    }

    private UnsupportedOperationException unsupported(String action) {
        return new UnsupportedOperationException(String.format(
                "Vodqa %s is not supported on web for WEB_ENGINE=%s.",
                action, ENGINE_NAME));
    }
}
