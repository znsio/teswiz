package com.znsio.teswiz.screen.jiomeet;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.jiomeet.InAMeetingScreenAndroid;
import com.znsio.teswiz.screen.web.jiomeet.InAMeetingScreenWeb;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class InAMeetingScreen {
    private static final String SCREEN_NAME = InAMeetingScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static InAMeetingScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(InAMeetingScreen.class);
    }

    public abstract boolean isMeetingStarted();

    public abstract String getMeetingId();

    public abstract String getMeetingPassword();

    public abstract InAMeetingScreen unmute();

    public abstract InAMeetingScreen mute();

    public abstract String getMicLabelText();

    public abstract InAMeetingScreen openJioMeetNotification();
}
