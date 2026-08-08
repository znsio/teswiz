package com.znsio.teswiz.screen.web.theapp;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.theapp.EchoScreen;

public class EchoScreenWeb
        extends EchoScreen {
    private static final String ENGINE_NAME = "selenium";

    public EchoScreenWeb(Driver driver, Visual visually) {
    }

    @Override
    public EchoScreen echoMessage(String message) {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(String.format(
                "TheApp Echo screen is not supported on web for WEB_ENGINE=%s.",
                ENGINE_NAME));
    }
}
