package com.znsio.sample.e2e.screen.web;

import com.znsio.e2e.runner.Driver;
import com.znsio.e2e.runner.Visual;
import com.znsio.sample.e2e.screen.ScreenShotScreen;

public class ScreenShotScreenWeb
        extends ScreenShotScreen {
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = ScreenShotScreenWeb.class.getSimpleName();

    public ScreenShotScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public ScreenShotScreen takeScreenshot() {
        visually.checkWindow(SCREEN_NAME, "Take Screenshot");
        return this;
    }
}
