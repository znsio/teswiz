package com.znsio.teswiz.screen.web.playwrightjava.jiomeet;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.jiomeet.InAMeetingScreen;
import com.znsio.teswiz.screen.jiomeet.LandingScreen;
import com.znsio.teswiz.screen.jiomeet.SignInScreen;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

public class SignInScreenPlaywrightJava extends SignInScreen {
    private static final String SCREEN_NAME = SignInScreenPlaywrightJava.class.getSimpleName();

    private static final By SIGN_IN_LINK = By.xpath("//a[text()='Sign In']");
    private static final By WELCOME_BACK_IMAGE = By.xpath("//img[contains(@class, 'signin-banner')]");
    private static final By USERNAME = By.id("username");
    private static final By PROCEED_BUTTON = By.id("proceedButton");
    private static final By PASSWORD = By.id("password");
    private static final By SIGN_IN_BUTTON = By.id("signinButton");
    private static final By JOIN_MEETING_BUTTON = By.id("headerJoinMeetingButton");
    private static final By MEETING_ID = By.id("meetingId");
    private static final By MEETING_PASSWORD = By.id("pin");
    private static final By PARTICIPANT_NAME = By.id("name");
    private static final By JOIN_BUTTON = By.xpath("//button[contains(text(), 'Join')]");

    private final Driver driver;
    private final Visual visually;

    public SignInScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public LandingScreen signIn(String username, String password) {
        driver.waitTillElementIsPresent(SIGN_IN_LINK).click();
        visually.checkWindow(SCREEN_NAME, "Start signin");
        driver.waitTillElementIsPresent(WELCOME_BACK_IMAGE);
        replaceText(USERNAME, username);
        driver.waitTillElementIsPresent(PROCEED_BUTTON).click();
        replaceText(PASSWORD, password);
        visually.checkWindow(SCREEN_NAME, "Credentials entered");
        driver.waitTillElementIsPresent(SIGN_IN_BUTTON).click();
        return LandingScreen.get();
    }

    @Override
    public InAMeetingScreen joinAMeeting(String meetingId, String meetingPassword, String currentUserPersona) {
        driver.waitTillElementIsPresent(JOIN_MEETING_BUTTON).click();
        visually.checkWindow(SCREEN_NAME, "Landing screen");
        replaceText(MEETING_ID, meetingId);
        replaceText(MEETING_PASSWORD, meetingPassword);
        replaceText(PARTICIPANT_NAME, currentUserPersona);
        visually.checkWindow(SCREEN_NAME, "After entering meeting details");
        clickJoinButton();
        InAMeetingScreen inAMeetingScreen = InAMeetingScreen.get();
        inAMeetingScreen.getMicLabelText();
        return inAMeetingScreen;
    }

    private void replaceText(By locator, String value) {
        WebElement element = driver.waitTillElementIsPresent(locator);
        element.clear();
        element.sendKeys(value);
    }

    private void clickJoinButton() {
        WebElement joinButton = driver.waitForClickabilityOf(JOIN_BUTTON);
        ((JavascriptExecutor) driver.getInnerDriver()).executeScript("arguments[0].click()", joinButton);
    }
}
