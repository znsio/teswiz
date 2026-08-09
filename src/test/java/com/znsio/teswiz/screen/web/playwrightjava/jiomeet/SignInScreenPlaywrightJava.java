package com.znsio.teswiz.screen.web.playwrightjava.jiomeet;

import com.microsoft.playwright.Locator;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.jiomeet.InAMeetingScreen;
import com.znsio.teswiz.screen.jiomeet.LandingScreen;
import com.znsio.teswiz.screen.jiomeet.SignInScreen;
import com.znsio.teswiz.web.playwright.screen.PlaywrightJavaScreenContext;

public class SignInScreenPlaywrightJava extends SignInScreen {
    private static final String SCREEN_NAME = SignInScreenPlaywrightJava.class.getSimpleName();

    private static final String SIGN_IN_LINK = "a:has-text(\"Sign In\")";
    private static final String WELCOME_BACK_IMAGE = "img[class*='signin-banner']";
    private static final String USERNAME = "#username";
    private static final String PROCEED_BUTTON = "#proceedButton";
    private static final String PASSWORD = "#password";
    private static final String SIGN_IN_BUTTON = "#signinButton";
    private static final String JOIN_MEETING_BUTTON = "#headerJoinMeetingButton";
    private static final String MEETING_ID = "#meetingId";
    private static final String MEETING_PASSWORD = "#pin";
    private static final String PARTICIPANT_NAME = "#name";
    private static final String JOIN_BUTTON = "button:has-text(\"Join\")";

    private final PlaywrightJavaScreenContext context;
    private final Visual visually;

    public SignInScreenPlaywrightJava(PlaywrightJavaScreenContext context) {
        this.context = context;
        this.visually = context.visual();
    }

    @Override
    public LandingScreen signIn(String username, String password) {
        context.page().locator(SIGN_IN_LINK).click();
        visually.checkWindow(SCREEN_NAME, "Start signin");
        context.page().locator(WELCOME_BACK_IMAGE).waitFor();
        replaceText(USERNAME, username);
        context.page().locator(PROCEED_BUTTON).click();
        replaceText(PASSWORD, password);
        visually.checkWindow(SCREEN_NAME, "Credentials entered");
        context.page().locator(SIGN_IN_BUTTON).click();
        return LandingScreen.get();
    }

    @Override
    public InAMeetingScreen joinAMeeting(String meetingId, String meetingPassword, String currentUserPersona) {
        context.page().locator(JOIN_MEETING_BUTTON).click();
        visually.checkWindow(SCREEN_NAME, "Landing screen");
        replaceText(MEETING_ID, meetingId);
        replaceText(MEETING_PASSWORD, meetingPassword);
        replaceText(PARTICIPANT_NAME, currentUserPersona);
        visually.checkWindow(SCREEN_NAME, "After entering meeting details");
        context.page().locator(JOIN_BUTTON).click();
        InAMeetingScreen inAMeetingScreen = InAMeetingScreen.get();
        inAMeetingScreen.getMicLabelText();
        return inAMeetingScreen;
    }

    private void replaceText(String selector, String value) {
        Locator locator = context.page().locator(selector);
        locator.fill(value);
    }
}
