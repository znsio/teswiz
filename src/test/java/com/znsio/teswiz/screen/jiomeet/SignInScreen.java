package com.znsio.teswiz.screen.jiomeet;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.jiomeet.SignInScreenAndroid;
import com.znsio.teswiz.screen.web.jiomeet.SignInScreenWeb;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class SignInScreen {
    private static final String SCREEN_NAME = SignInScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static SignInScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(SignInScreen.class);
    }

    public abstract LandingScreen signIn(String username, String password);

    public abstract InAMeetingScreen joinAMeeting(String meetingId, String meetingPassword,
                                                  String currentUserPersona);
}
