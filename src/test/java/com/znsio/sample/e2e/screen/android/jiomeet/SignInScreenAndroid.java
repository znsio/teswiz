package com.znsio.sample.e2e.screen.android.jiomeet;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.jiomeet.InAMeetingScreen;
import com.znsio.sample.e2e.screen.jiomeet.LandingScreen;
import com.znsio.sample.e2e.screen.jiomeet.SignInScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.znsio.e2e.tools.Wait.waitFor;

public class SignInScreenAndroid
        extends SignInScreen {
    private final Driver driver;
    private final Visual visually;
    private static final String SCREEN_NAME = SignInScreenAndroid.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private final By bySignInId = By.id("com.jio.rilconferences:id/signIn");
    private final By byUserNameId = By.id("com.jio.rilconferences:id/edit_mobile_number");
    private final By byProceedButtonId = By.id("com.jio.rilconferences:id/btn_otp_next");
    private final By byPasswordId = By.id("com.jio.rilconferences:id/edt_otp_number");
    private final By byWelcomeMessageId = By.id("com.jio.rilconferences:id/textUserName");
    private final By byJoinMeetingId = By.id("com.jio.rilconferences:id/joinMeetingBtn");
    private final By byEnterMeetingId = By.id("com.jio.rilconferences:id/inputMeetingLink");
    private final By byEnterMeetingPasswordId = By.id("com.jio.rilconferences:id/inputMeetingPassword");
    private final By byEnterDisplayNameId = By.id("com.jio.rilconferences:id/inputUserName");
    private final By byEnterMeeting = By.id("com.jio.rilconferences:id/buttonNext");
    private final By byJoinMeeting = By.id("com.jio.rilconferences:id/buttonJoinMeeting");

    public SignInScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public LandingScreen signIn(String username, String password) {
        startSignInProcess().enterUsernameAndProceed(username)
                            .enterPasswordAndSignIn(password);

        return LandingScreen.get()
                            .waitTillWelcomeMessageIsSeen();
    }

    @Override
    public InAMeetingScreen joinAMeeting(String meetingId, String meetingPassword, String currentUserPersona) {
        joinAMeeting().enterMeetingDetails(meetingId, meetingPassword, currentUserPersona);
        return InAMeetingScreen.get();
    }

    private SignInScreenAndroid joinAMeeting() {
        visually.checkWindow(SCREEN_NAME, "joinAMeeting");
        driver.waitTillElementIsPresent(byJoinMeetingId)
              .click();
        return this;
    }

    private SignInScreenAndroid enterMeetingDetails(String meetingId, String meetingPassword, String currentUserPersona) {
        driver.waitTillElementIsPresent(byEnterMeetingId)
              .sendKeys(meetingId);
        driver.waitTillElementIsPresent(byEnterMeetingPasswordId)
              .sendKeys(meetingPassword);
        driver.waitTillElementIsPresent(byEnterDisplayNameId)
              .sendKeys(currentUserPersona);
        driver.waitTillElementIsPresent(byEnterMeeting)
              .click();
        visually.checkWindow(SCREEN_NAME, "enterMeetingDetails");
        driver.waitTillElementIsPresent(byJoinMeeting)
              .click();
        waitFor(8);
        return this;
    }

    private SignInScreenAndroid enterPasswordAndSignIn(String password) {
        WebElement passwordElement = driver.waitTillElementIsPresent(byPasswordId);
        passwordElement.click();
        passwordElement.clear();
        passwordElement.sendKeys(password);
        visually.checkWindow(SCREEN_NAME, "Password entered");
        driver.waitForClickabilityOf(byProceedButtonId)
              .click();
        return this;
    }

    private SignInScreenAndroid enterUsernameAndProceed(String username) {
        WebElement usernameElement = driver.waitTillElementIsPresent(byUserNameId);
        usernameElement.click();
        usernameElement.clear();
        usernameElement.sendKeys(username);
        visually.checkWindow(SCREEN_NAME, "Username entered");
        driver.waitForClickabilityOf(byProceedButtonId)
              .click();
        return this;
    }

    private SignInScreenAndroid startSignInProcess() {
        visually.checkWindow(SCREEN_NAME, "Start Sign In process");
        driver.waitTillElementIsPresent(bySignInId)
              .click();
        return this;
    }
}
