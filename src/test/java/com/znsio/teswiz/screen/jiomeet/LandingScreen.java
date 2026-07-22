package com.znsio.teswiz.screen.jiomeet;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.jiomeet.LandingScreenAndroid;
import com.znsio.teswiz.screen.web.jiomeet.LandingScreenWeb;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class LandingScreen {
    private static final String SCREEN_NAME = LandingScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static LandingScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(LandingScreen.class);
    }

    public abstract String getSignedInWelcomeMessage();

    public abstract InAMeetingScreen startInstantMeeting();

    public abstract LandingScreen waitTillWelcomeMessageIsSeen();
}
