package com.znsio.teswiz.screen.web.playwrightjava;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ScreenShotScreen;

import static com.znsio.teswiz.tools.Wait.waitFor;

public class ScreenShotScreenPlaywrightJava extends ScreenShotScreen {
    private static final String SCREEN_NAME = ScreenShotScreenPlaywrightJava.class.getSimpleName();
    private static final String PAGE_URL = "https://github.com/znsio/teswiz";

    private final Driver driver;
    private final Visual visually;

    public ScreenShotScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public ScreenShotScreen takeScreenshot() {
        visually.checkWindow(SCREEN_NAME, "Take Screenshot");
        driver.getInnerDriver().get(PAGE_URL);
        waitFor(3);
        visually.checkWindow(SCREEN_NAME, "teswiz");
        return this;
    }
}
