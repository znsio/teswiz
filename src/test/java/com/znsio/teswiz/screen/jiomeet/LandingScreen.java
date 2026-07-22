package com.znsio.teswiz.screen.jiomeet;


public abstract class LandingScreen {

    public static LandingScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(LandingScreen.class);
    }

    public abstract String getSignedInWelcomeMessage();

    public abstract InAMeetingScreen startInstantMeeting();

    public abstract LandingScreen waitTillWelcomeMessageIsSeen();
}
