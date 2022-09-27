package com.znsio.sample.e2e.screen.android.jiomeet;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.jiomeet.InAMeetingScreen;
import com.znsio.sample.e2e.screen.jiomeet.LandingScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

import static com.znsio.e2e.tools.Wait.waitFor;

public class LandingScreenAndroid
        extends LandingScreen {
    private final Driver driver;
    private final Visual visually;
    private static final String SCREEN_NAME = LandingScreenAndroid.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private final By byWelcomeMessageId = By.id("com.jio.rilconferences:id/textUserName");
    private final By byStartInstantMeetingId = By.id("com.jio.rilconferences:id/buttonStartMeeting");

    public LandingScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public String getSignedInWelcomeMessage() {
        return driver.waitTillElementIsPresent(byWelcomeMessageId)
                     .getText();
    }

    @Override
    public InAMeetingScreen startInstantMeeting() {
        visually.checkWindow(SCREEN_NAME, "Start instant meeting");
        driver.waitTillElementIsPresent(byStartInstantMeetingId)
              .click();
        visually.checkWindow(SCREEN_NAME, "Options before starting meeting");
        driver.waitTillElementIsPresent(byStartInstantMeetingId)
              .click();
        waitFor(8);
        return InAMeetingScreen.get();
    }

    @Override
    public LandingScreen waitTillWelcomeMessageIsSeen() {
        driver.waitTillElementIsPresent(byWelcomeMessageId);
        return this;
    }
}
