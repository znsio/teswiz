package com.znsio.teswiz.screen.web.playwrightjava.vodqa;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.vodqa.NativeViewScreen;

public class NativeViewScreenPlaywrightJava extends NativeViewScreen {
    private static final String ENGINE_NAME = "playwright-java";

    public NativeViewScreenPlaywrightJava(Driver driver, Visual visually) {
    }

    @Override
    public boolean isUserOnNativeViewScreen() {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(String.format(
                "Vodqa native view is not supported on web for WEB_ENGINE=%s.",
                ENGINE_NAME));
    }
}
