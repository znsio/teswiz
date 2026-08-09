package com.znsio.teswiz.screen.web.playwrightjava.jiomeet;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.jiomeet.InAMeetingScreen;
import com.znsio.teswiz.web.playwright.screen.PlaywrightJavaScreenContext;
import org.apache.commons.lang3.NotImplementedException;

public class InAMeetingScreenPlaywrightJava extends InAMeetingScreen {
    private static final String SCREEN_NAME = InAMeetingScreenPlaywrightJava.class.getSimpleName();

    private static final String MEETING_INFO_ICON = "div.icon.pointer";
    private static final String MIC_LABEL = "div[class*='mic-section'] img";
    private static final String CURRENT_MEETING_NUMBER = "xpath=//div[text()='Meeting ID']/following-sibling::div";
    private static final String CURRENT_MEETING_PASSWORD = "xpath=//div[text()='Password']/following-sibling::div";
    private static final String MICROPHONE_BUTTON = "#toggleMicButton div[class*='img-holder']";

    private final PlaywrightJavaScreenContext context;
    private final Visual visually;
    private final TestExecutionContext testExecutionContext;

    public InAMeetingScreenPlaywrightJava(PlaywrightJavaScreenContext context) {
        this.context = context;
        this.visually = context.visual();
        this.testExecutionContext = Runner.getTestExecutionContext(Thread.currentThread().getId());
    }

    @Override
    public boolean isMeetingStarted() {
        getMicLabelText();
        return true;
    }

    @Override
    public String getMeetingId() {
        String meetingId = readMeetingDetail(CURRENT_MEETING_NUMBER);
        testExecutionContext.addTestState(SAMPLE_TEST_CONTEXT.MEETING_ID, meetingId);
        return meetingId;
    }

    @Override
    public String getMeetingPassword() {
        String meetingPassword = readMeetingDetail(CURRENT_MEETING_PASSWORD);
        testExecutionContext.addTestState(SAMPLE_TEST_CONTEXT.MEETING_PASSWORD, meetingPassword);
        return meetingPassword;
    }

    @Override
    public InAMeetingScreen unmute() {
        revealMeetingControls();
        context.page().locator(MICROPHONE_BUTTON).click();
        visually.checkWindow(SCREEN_NAME, "Mic is unmuted");
        return this;
    }

    @Override
    public InAMeetingScreen mute() {
        revealMeetingControls();
        context.page().locator(MICROPHONE_BUTTON).click();
        visually.checkWindow(SCREEN_NAME, "Mic is muted");
        return this;
    }

    @Override
    public String getMicLabelText() {
        revealMeetingControls();
        String micLabelText = context.page().locator(MIC_LABEL).getAttribute("src");
        if (null == micLabelText) {
            micLabelText = "";
        }
        micLabelText = micLabelText.trim();
        visually.takeScreenshot(SCREEN_NAME, "in a meeting after micLabel text");
        return micLabelText;
    }

    @Override
    public InAMeetingScreen openJioMeetNotification() {
        throw new NotImplementedException("Jio Meet Device Notification of Meeting is not available for Web");
    }

    private String readMeetingDetail(String selector) {
        context.page().locator(MEETING_INFO_ICON).click();
        visually.takeScreenshot(SCREEN_NAME, "getCurrentMeetingDetails");
        String detail = context.page().locator(selector).innerText();
        context.page().locator(MEETING_INFO_ICON).click();
        visually.takeScreenshot(SCREEN_NAME, "After closing meeting info icon");
        return detail.replaceAll("\\s", "");
    }

    private void revealMeetingControls() {
        context.page().locator(MEETING_INFO_ICON).hover();
    }
}
