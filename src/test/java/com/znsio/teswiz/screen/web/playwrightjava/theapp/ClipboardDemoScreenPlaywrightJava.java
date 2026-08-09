package com.znsio.teswiz.screen.web.playwrightjava.theapp;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.theapp.ClipboardDemoScreen;

public class ClipboardDemoScreenPlaywrightJava extends ClipboardDemoScreen {
    private static final String ENGINE_NAME = "playwright-java";

    public ClipboardDemoScreenPlaywrightJava(Driver driver, Visual visually) {
    }

    @Override
    public ClipboardDemoScreen setInClipboard(String content) {
        throw unsupported();
    }

    @Override
    public boolean doesAddedContentExistInClipboard() {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(String.format(
                "TheApp Clipboard Demo screen is not supported on web for WEB_ENGINE=%s.",
                ENGINE_NAME));
    }
}
