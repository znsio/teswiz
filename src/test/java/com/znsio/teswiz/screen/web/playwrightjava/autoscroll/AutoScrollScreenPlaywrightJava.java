package com.znsio.teswiz.screen.web.playwrightjava.autoscroll;

import com.znsio.teswiz.entities.Direction;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.autoscroll.AutoScrollScreen;

public class AutoScrollScreenPlaywrightJava extends AutoScrollScreen {
    private static final String ENGINE_NAME = "playwright-java";

    public AutoScrollScreenPlaywrightJava(Driver driver, Visual visually) {
    }

    @Override
    public AutoScrollScreen goToDropdownWindow() {
        throw unsupported();
    }

    @Override
    public AutoScrollScreen scrollInDynamicLayer(Direction direction) {
        throw unsupported();
    }

    @Override
    public boolean isScrollSuccessful() {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(String.format(
                "AutoScroll is not supported on web for WEB_ENGINE=%s.",
                ENGINE_NAME));
    }
}
