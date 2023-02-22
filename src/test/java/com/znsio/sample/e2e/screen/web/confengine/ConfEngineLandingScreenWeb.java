package com.znsio.sample.e2e.screen.web.confengine;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.confengine.ConfEngineLandingScreen;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;

public class ConfEngineLandingScreenWeb
        extends ConfEngineLandingScreen {
    private static final String SCREEN_NAME = ConfEngineLandingScreenWeb.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private static final String NOT_YET_IMPLEMENTED = " not yet implemented";
    private final Driver driver;
    private final Visual visually;

    public ConfEngineLandingScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        visually.checkWindow(SCREEN_NAME, "Launch screen");
    }

    @Override
    public ConfEngineLandingScreen getListOfConferences() {
        throw new NotImplementedException(
                SCREEN_NAME + ":" + new Throwable().getStackTrace()[0].getMethodName() + NOT_YET_IMPLEMENTED);
    }
}
