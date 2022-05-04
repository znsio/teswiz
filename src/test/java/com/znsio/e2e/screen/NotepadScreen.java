package com.znsio.e2e.screen;

import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.screen.android.NotepadScreenAndroid;
import com.znsio.e2e.screen.web.NotepadScreenWeb;
import com.znsio.e2e.screen.windows.NotepadScreenWindows;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.log4j.Logger;

import static com.znsio.e2e.runner.Runner.fetchDriver;
import static com.znsio.e2e.runner.Runner.fetchEyes;

public abstract class NotepadScreen {
    private static final String SCREEN_NAME = NotepadScreen.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);

    public static NotepadScreen get() {
        Driver driver = fetchDriver(Thread.currentThread().getId());
        Platform platform = Runner.fetchPlatform(Thread.currentThread().getId());
        LOGGER.info(SCREEN_NAME + ": Driver type: " + driver.getType() + ": Platform: " + platform);
        Visual visually = fetchEyes(Thread.currentThread().getId());

        switch (platform) {
            case android:
                return new NotepadScreenAndroid(driver, visually);
            case windows:
                return new NotepadScreenWindows(driver, visually);
            case web:
                return new NotepadScreenWeb(driver, visually);
        }
        throw new NotImplementedException(SCREEN_NAME + " is not implemented in " + Runner.platform);
    }

    public abstract NotepadScreen takeScreenshot();

    public abstract NotepadScreen typeMessage(String message);
}
