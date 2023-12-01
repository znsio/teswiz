package com.znsio.teswiz.screen.web.jiomeet;

import com.applitools.eyes.selenium.fluent.Target;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.jiomeet.InAMeetingScreen;
import com.znsio.teswiz.screen.jiomeet.LandingScreen;
import com.znsio.teswiz.screen.jiomeet.SignInScreen;
import com.znsio.teswiz.tools.Wait;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class SignInScreenWeb
        extends SignInScreen {
    private static final String SCREEN_NAME = SignInScreenWeb.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private static final String NOT_YET_IMPLEMENTED = " not yet implemented";
    private static final By byEnterMeetingId = By.id("meetingId");
    private static final By byJoinMeetingButtonId = By.id("headerJoinMeetingButton");
    private static final By byEnterPasswordId = By.id("pin");
    private static final By byNameId = By.id("name");
    private static final By byJoinMeetingButtonXpath = By.xpath(
            "//button[contains(text(), 'Join')]");
    private static final By bySignInXpath = By.xpath("//a[text()='Sign In']");
    private static final By byUsernameId = By.id("username");
    private static final By byProceedButtonId = By.id("proceedButton");
    private static final By byPasswordId = By.id("password");
    private static final By bySigninButtonId = By.id("signinButton");
    private final Driver driver;
    private final Visual visually;

    public SignInScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public LandingScreen signIn(String username, String password) {

        driver.waitTillElementIsPresent(bySignInXpath).click();
        visually.checkWindow(SCREEN_NAME, "Start signin");
        if (driver.isElementPresent(By.xpath("//*[contains(@class, 'signin-banner ng-star-inserted')]"))) {
            for (int i = 0; i < 5; i++) {
                driver.waitTillElementIsPresent(By.xpath("//*[contains(@class, 'signin-banner ng-star-inserted')]")).click();
            }
            if (driver.isElementPresent(By.xpath("//*[contains(@class,'env-select ng')]"))) {
                driver.waitTillElementIsPresent(By.xpath("//*[contains(@class,'env-select ng')]")).click();
                WebElement dropdownElement = driver.findElement(By.xpath("//*[contains(@class,'env-select ng')]"));
                Select select = new Select(dropdownElement);
                select.selectByVisibleText("RC");
            }
        }

        WebElement usernameElement = driver.waitTillElementIsPresent(byUsernameId);
        usernameElement.clear();
        usernameElement.sendKeys(username);

        driver.waitTillElementIsPresent(byProceedButtonId).click();

        WebElement passwordElement = driver.waitTillElementIsPresent(byPasswordId);
        passwordElement.clear();
        passwordElement.sendKeys(password);

        visually.checkWindow(SCREEN_NAME, "Credentials entered");

        driver.waitTillElementIsPresent(bySigninButtonId).click();
        return LandingScreen.get();
    }

    @Override
    public InAMeetingScreen joinAMeeting(String meetingId, String meetingPassword,
                                         String currentUserPersona) {
        Wait.waitFor(5);
        if (driver.isElementPresent(bySignInXpath)) {
            driver.waitTillElementIsPresent(bySignInXpath).click();
            if (driver.isElementPresent(By.xpath("//*[contains(@class, 'signin-banner')]"))) {
                for (int i = 0; i < 5; i++) {
                    driver.waitTillElementIsPresent(By.xpath("//*[contains(@class, 'signin-banner')]")).click();
                }
                if (driver.isElementPresent(By.xpath("//*[contains(@class,'env-select ng')]"))) {
                    driver.waitTillElementIsPresent(By.xpath("//*[contains(@class,'env-select ng')]")).click();
                    WebElement dropdownElement = driver.findElement(By.xpath("//*[contains(@class,'env-select ng')]"));
                    Select select = new Select(dropdownElement);
                    select.selectByVisibleText("RC");
                }
            }

            WebElement joinMeetingElement = driver.waitTillElementIsPresent(byJoinMeetingButtonId);
            visually.checkWindow(SCREEN_NAME, "Landing screen");
            joinMeetingElement.click();

            WebElement enterMeetingIdElement = driver.waitTillElementIsPresent(byEnterMeetingId);
            enterMeetingIdElement.clear();
            enterMeetingIdElement.sendKeys(meetingId);

            WebElement enterPasswordElement = driver.waitTillElementIsPresent(byEnterPasswordId);
            enterPasswordElement.clear();
            enterPasswordElement.sendKeys(meetingPassword);

            WebElement enterNameElement = driver.waitTillElementIsPresent(byNameId);
            enterNameElement.clear();
            enterNameElement.sendKeys(currentUserPersona);

            visually.check(SCREEN_NAME, "After entering meeting details",
                    Target.window().strict().layout(byEnterPasswordId).layout(byNameId));

            visually.takeScreenshot(SCREEN_NAME, "Before clicking on Join button");
            ((JavascriptExecutor) driver.getInnerDriver()).executeScript("arguments[0].click()",
                    driver.waitForClickabilityOf(
                            byJoinMeetingButtonXpath));

        }
        return this.waitForInAMeetingScreenToLoad();
    }

    private InAMeetingScreen waitForInAMeetingScreenToLoad() {
        InAMeetingScreen inAMeetingScreen = InAMeetingScreen.get();
        inAMeetingScreen.getMicLabelText();
        return inAMeetingScreen;
    }
}
