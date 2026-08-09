package com.znsio.teswiz.screen.web.playwrightjava.duckduckgo;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.duckduckgo.DuckDuckGoScreen;

public class DuckDuckGoScreenPlaywrightJava extends DuckDuckGoScreen {
    private static final String ENGINE_NAME = "playwright-java";

    public DuckDuckGoScreenPlaywrightJava(Driver driver, Visual visually) {
    }

    @Override
    public DuckDuckGoScreen launchBrowser() {
        throw unsupported();
    }

    @Override
    public DuckDuckGoScreen cancelChangingDefaultBrowserPopup() {
        throw unsupported();
    }

    @Override
    public String getDefaultTextFromWebView() {
        throw unsupported();
    }

    @Override
    public DuckDuckGoScreen switchToNativeContextAndGoToTeswizGithub() {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(String.format(
                "DuckDuckGo is not supported on web for WEB_ENGINE=%s.",
                ENGINE_NAME));
    }
}
