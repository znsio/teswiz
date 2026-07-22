package com.znsio.teswiz.screen.vodqa;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.vodqa.DragAndDropScreenAndroid;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DragAndDropScreen {

    private static final String SCREEN_NAME = DragAndDropScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static DragAndDropScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(DragAndDropScreen.class);
    }

    public abstract boolean isMessageVisible();
    public abstract DragAndDropScreen dragAndDropCircleObject();
}
