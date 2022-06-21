package com.znsio.sample.e2e.screen;

import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.android.ScreenShotScreenAndroid;
import com.znsio.sample.e2e.screen.web.ScreenShotScreenWeb;
import com.znsio.sample.e2e.screen.windows.ScreenShotScreenWindows;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;

import static com.znsio.e2e.runner.Runner.fetchDriver;
import static com.znsio.e2e.runner.Runner.fetchEyes;

public abstract class ScreenShotScreen {
    private static final String SCREEN_NAME = ScreenShotScreen.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);

    public static ScreenShotScreen get() {
        Driver driver = fetchDriver(Thread.currentThread()
                                          .getId());
        Platform platform = Runner.fetchPlatform(Thread.currentThread()
                                                       .getId());
        LOGGER.info(SCREEN_NAME + ": Driver type: " + driver.getType() + ": Platform: " + platform);
        Visual visually = fetchEyes(Thread.currentThread()
                                          .getId());

        switch(platform) {
            case android:
                return new ScreenShotScreenAndroid(driver, visually);
            case web:
                return new ScreenShotScreenWeb(driver, visually);
            case windows:
                return new ScreenShotScreenWindows(driver, visually);
        }
        throw new NotImplementedException(SCREEN_NAME + " is not implemented in " + Runner.platform);
    }

    public abstract ScreenShotScreen takeScreenshot();
}
