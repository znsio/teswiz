package com.znsio.teswiz.screen.web.playwrightjava.jiomeet;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.jiomeet.InAMeetingScreen;
import com.znsio.teswiz.screen.jiomeet.LandingScreen;
import org.openqa.selenium.By;

public class LandingScreenPlaywrightJava extends LandingScreen {
    private static final String SCREEN_NAME = LandingScreenPlaywrightJava.class.getSimpleName();

    private static final By HEADING = By.xpath("//h3[contains(@class,'heading')]");
    private static final By DESCRIPTION = By.xpath("//p[@class='desc']");
    private static final By START_MEETING_OPTION = By.xpath("//div[contains(text(), 'Start a Meeting')]");
    private static final By START_MEETING_BUTTON = By.xpath("//button[contains(text(), 'Start')]");

    private final Driver driver;
    private final Visual visually;

    public LandingScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public String getSignedInWelcomeMessage() {
        visually.checkWindow(SCREEN_NAME, "get signedin welcome message");
        String heading = driver.waitTillElementIsPresent(HEADING).getText();
        String description = driver.waitTillElementIsPresent(DESCRIPTION).getText();
        return String.format("%s %s", heading, description).trim();
    }

    @Override
    public InAMeetingScreen startInstantMeeting() {
        driver.waitForClickabilityOf(START_MEETING_OPTION).click();
        visually.checkWindow(SCREEN_NAME, "Start meeting using 'One time Meeting ID'");
        driver.waitForClickabilityOf(START_MEETING_BUTTON).click();
        InAMeetingScreen inAMeetingScreen = InAMeetingScreen.get();
        inAMeetingScreen.getMicLabelText();
        visually.checkWindow(SCREEN_NAME, "Meeting started");
        return inAMeetingScreen;
    }

    @Override
    public LandingScreen waitTillWelcomeMessageIsSeen() {
        driver.waitTillElementIsPresent(HEADING);
        visually.checkWindow(SCREEN_NAME, "signedin successfully");
        return this;
    }
}
