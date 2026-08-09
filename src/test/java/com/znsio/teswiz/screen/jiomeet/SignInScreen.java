package com.znsio.teswiz.screen.jiomeet;


public abstract class SignInScreen {

    public static SignInScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(SignInScreen.class);
    }

    public abstract LandingScreen signIn(String username, String password);

    public abstract InAMeetingScreen joinAMeeting(String meetingId, String meetingPassword,
                                                  String currentUserPersona);
}
