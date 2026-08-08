package com.znsio.teswiz.screen.jiomeet;


public abstract class InAMeetingScreen {

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
