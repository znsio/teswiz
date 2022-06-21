package com.znsio.sample.e2e.screen.windows;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.ScreenShotScreen;

public class ScreenShotScreenWindows
        extends ScreenShotScreen {
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = ScreenShotScreenWindows.class.getSimpleName();

    public ScreenShotScreenWindows(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public ScreenShotScreen takeScreenshot() {
        visually.takeScreenshot(SCREEN_NAME, "Take Screenshot");
        return this;
    }
}
