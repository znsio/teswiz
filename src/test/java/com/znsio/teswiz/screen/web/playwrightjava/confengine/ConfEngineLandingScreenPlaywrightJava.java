package com.znsio.teswiz.screen.web.playwrightjava.confengine;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.confengine.ConfEngineLandingScreen;
import org.apache.commons.lang3.NotImplementedException;

public class ConfEngineLandingScreenPlaywrightJava extends ConfEngineLandingScreen {
    private static final String SCREEN_NAME = ConfEngineLandingScreenPlaywrightJava.class.getSimpleName();
    private static final String NOT_YET_IMPLEMENTED = " not yet implemented";

    private final Visual visually;

    public ConfEngineLandingScreenPlaywrightJava(Driver driver, Visual visually) {
        this.visually = visually;
        visually.checkWindow(SCREEN_NAME, "Launch screen");
    }

    @Override
    public ConfEngineLandingScreen getListOfConferences() {
        throw new NotImplementedException(
                SCREEN_NAME + ":" + new Throwable().getStackTrace()[0].getMethodName() + NOT_YET_IMPLEMENTED);
    }
}
