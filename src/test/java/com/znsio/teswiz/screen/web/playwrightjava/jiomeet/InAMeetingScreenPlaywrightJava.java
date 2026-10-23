package com.znsio.teswiz.screen.web.playwrightjava.jiomeet;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.jiomeet.InAMeetingScreen;
import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class InAMeetingScreenPlaywrightJava extends InAMeetingScreen {
    private static final String SCREEN_NAME = InAMeetingScreenPlaywrightJava.class.getSimpleName();

    private static final By MEETING_INFO_ICON = By.xpath("//div[@class='icon pointer']");
    private static final By MIC_LABEL = By.xpath("//div[contains(@class, 'mic-section')]//img");
    private static final By CURRENT_MEETING_NUMBER = By.xpath("//div[text()='Meeting ID']/following-sibling::div");
    private static final By CURRENT_MEETING_PASSWORD = By.xpath("//div[text()='Password']/following-sibling::div");
    private static final By MICROPHONE_BUTTON = By.xpath("//div[@id = 'toggleMicButton']//div[contains(@class, 'img-holder')]");

    private final Driver driver;
    private final Visual visually;
    private final TestExecutionContext context;

    public InAMeetingScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        this.context = Runner.getTestExecutionContext(Thread.currentThread().getId());
    }

    @Override
    public boolean isMeetingStarted() {
        getMicLabelText();
        return true;
    }

    @Override
    public String getMeetingId() {
        String meetingId = readMeetingDetail(CURRENT_MEETING_NUMBER);
        context.addTestState(SAMPLE_TEST_CONTEXT.MEETING_ID, meetingId);
        return meetingId;
    }

    @Override
    public String getMeetingPassword() {
        String meetingPassword = readMeetingDetail(CURRENT_MEETING_PASSWORD);
        context.addTestState(SAMPLE_TEST_CONTEXT.MEETING_PASSWORD, meetingPassword);
        return meetingPassword;
    }

    @Override
    public InAMeetingScreen unmute() {
        revealMeetingControls();
        driver.waitTillElementIsPresent(MICROPHONE_BUTTON).click();
        visually.checkWindow(SCREEN_NAME, "Mic is unmuted");
        return this;
    }

    @Override
    public InAMeetingScreen mute() {
        revealMeetingControls();
        driver.waitTillElementIsPresent(MICROPHONE_BUTTON).click();
        visually.checkWindow(SCREEN_NAME, "Mic is muted");
        return this;
    }

    @Override
    public String getMicLabelText() {
        revealMeetingControls();
        String micLabelText = driver.waitTillElementIsPresent(MIC_LABEL).getText().trim();
        visually.takeScreenshot(SCREEN_NAME, "in a meeting after micLabel text");
        return micLabelText;
    }

    @Override
    public InAMeetingScreen openJioMeetNotification() {
        throw new NotImplementedException("Jio Meet Device Notification of Meeting is not available for Web");
    }

    private String readMeetingDetail(By locator) {
        WebElement infoIcon = driver.waitTillElementIsPresent(MEETING_INFO_ICON, 20);
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver.getInnerDriver();
        javascriptExecutor.executeScript("arguments[0].click()", infoIcon);
        visually.takeScreenshot(SCREEN_NAME, "getCurrentMeetingDetails");
        String detail = (String) javascriptExecutor.executeScript("return arguments[0].innerText",
                driver.waitTillElementIsPresent(locator));
        javascriptExecutor.executeScript("arguments[0].click()", infoIcon);
        visually.takeScreenshot(SCREEN_NAME, "After closing meeting info icon");
        return detail.replaceAll("\\s", "");
    }

    private void revealMeetingControls() {
        new Actions(driver.getInnerDriver())
                .moveToElement(driver.waitForClickabilityOf(MEETING_INFO_ICON))
                .moveByOffset(25, 25)
                .perform();
    }
}
