package com.znsio.teswiz.screen.web.jiomeet;

import com.context.TestExecutionContext;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.jiomeet.InAMeetingScreen;
import com.znsio.teswiz.tools.ReportPortalLogger;
import com.znsio.teswiz.tools.Wait;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.List;

public class InAMeetingScreenWeb
        extends InAMeetingScreen {
    private static final String SCREEN_NAME = InAMeetingScreenWeb.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private static final String NOT_YET_IMPLEMENTED = " not yet implemented";
    private static final By byMeetingInfoIconXpath = By.xpath("//div[@class='icon pointer']");
    private static final By byMicLabelXpath = By.xpath("//div[contains(@class, 'mic-section')]//img");
    private static final By byCurrentMeetingNumberXpath = By.xpath(
            "//div[contains(@class, 'card-panel ng')]/descendant::li[3]/child::div/child::div[contains(text(), 'Meeting ID')]/following-sibling::div");
    private static final By byCurrentMeetingPinXpath = By.xpath(
            "//div[contains(@class, 'card-panel ng')]/descendant::ul/descendant::div[contains(text(), 'Password') and contains(@class, 'name')]/following-sibling::div");
    private static final By byCurrentMeetingInvitationLinkXpath = By.xpath(
            "//div[text()='Invitation Link']/following-sibling::div");
    private final By byChatIconXpath = By.xpath("//div[text()='Chat']");
    private final By byPrivateChatContainerCssSelector = By.cssSelector("div.private-chat-container");
    private final By byChatMessageTextXpath = By.xpath("//div[@id='msg-area']");
    private static final String CHAT_MESSAGE_XPATH = "//div[@id='msg-area' and contains(text(), '@chatMessage')]";
    private final By byMessageTextBoxCssSelector = By.cssSelector("div.ql-editor");
    private final By byGroupChatContainerCssSelector = By.cssSelector("div.group-chat-conponent");
    private final By bySendMessageButtonCssSelector = By.cssSelector("div.sendBtn");
    private final By byChatNotificationRedBubbleXpath = By.xpath("//img[contains(@src, 'chat_recievied')]");
    private final By byLeaveButtonXpath = By.cssSelector("div.close-btn");
    private final By bySkipFeedbackButtonId = By.id("skipButton");
    private final By CHATS_TAB_XPATH = By.xpath("//span[contains(@class, 'header') and contains(text(), 'Chats')]");
    private final By byMostRecentChatWindowXpath = By.xpath("//div[@id='chats']/div");
    private final By bySendButtonCssSelector = By.cssSelector("div.sendBtn>img");
    private final By byChatMessagesInChatsTabXpath = By.xpath("//div[contains(@class, 'item-text')]");
    private final Driver driver;
    private final Visual visually;
    private final WebDriver innerDriver;
    private final TestExecutionContext context;

    public InAMeetingScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        this.innerDriver = this.driver.getInnerDriver();
        long threadId = Thread.currentThread().getId();
        context = Runner.getTestExecutionContext(threadId);
    }

    @Override
    public boolean isMeetingStarted() {
        LOGGER.info("Has meeting started? " + getMicLabelText());
        return true;
    }

    @Override
    public String getMeetingId() {
        WebElement infoIcon = driver.waitTillElementIsPresent(byMeetingInfoIconXpath, 20);
        JavascriptExecutor js = (JavascriptExecutor) innerDriver;
        js.executeScript("arguments[0].click()", infoIcon);
        visually.takeScreenshot(SCREEN_NAME, "getCurrentMeetingDetails");
        String meetingId = driver.waitForClickabilityOf(byCurrentMeetingNumberXpath).getText();
        meetingId = meetingId.replaceAll("\\s", "");
        String pin = driver.waitForClickabilityOf(byCurrentMeetingPinXpath).getText();
        String invitationLink = driver.waitForClickabilityOf(byCurrentMeetingInvitationLinkXpath)
                .getText();
        js.executeScript("arguments[0].click()", infoIcon);//to close the meeting info frame
        visually.takeScreenshot(SCREEN_NAME, "After closing meeting info icon");
        LOGGER.info("On Web the meeting id: " + meetingId + " Password: " + pin);

        context.addTestState(SAMPLE_TEST_CONTEXT.MEETING_ID, meetingId);
        context.addTestState(SAMPLE_TEST_CONTEXT.MEETING_PASSWORD, pin);
        context.addTestState(SAMPLE_TEST_CONTEXT.INVITATION_LINK, invitationLink);
        return meetingId;
    }

    @Override
    public String getMeetingPassword() {
        String pin = context.getTestStateAsString(SAMPLE_TEST_CONTEXT.MEETING_PASSWORD);
        return pin;
    }

    @Override
    public InAMeetingScreen unmute() {
        enableInMeetingControls("unmute");
        driver.waitTillElementIsPresent(By.xpath("//div[contains(@class,'mic-section')]/div[contains(@class, 'img-holder')]")).click();
        visually.checkWindow(SCREEN_NAME, "Mic is unmuted");
        return this;
    }
    @Override
    public InAMeetingScreen mute() {
        enableInMeetingControls("mute");
        driver.waitTillElementIsPresent(By.xpath("//div[contains(@class,'mic-section')]/div[contains(@class, 'img-holder')]")).click();
        visually.checkWindow(SCREEN_NAME, "Mic is muted");
        return this;
    }


    @Override
    public String getMicLabelText() {
        LOGGER.info("getMicLabelText");
        enableInMeetingControls("getMicLabelText");
        String micLabelText = driver.waitTillElementIsPresent(byMicLabelXpath).getText().trim();
        visually.takeScreenshot(SCREEN_NAME, "in a meeting after micLabel text");
        LOGGER.info("getMicLabelText: mic label text : " + micLabelText);
        return micLabelText;
    }

    @Override
    public InAMeetingScreen openJioMeetNotification() {
        throw new NotImplementedException("Jio Meet Device Notification of Meeting is not available for Web");
    }
    @Override
    public InAMeetingScreen userClicksOnChatWindow() {
        driver.waitForClickabilityOf(byChatIconXpath).click();
        visually.takeScreenshot(SCREEN_NAME, "tapped on Chat Icon");
        return this;
    }

    @Override
    public int getNumberOfMessages() {
        Wait.waitFor(2); // to get the count of messages
        //private chat is an overlap screen on group chat (i.e. 'To: All') - hence finding the concerned chat messages in case of private chat
        if (driver.isElementPresent(byPrivateChatContainerCssSelector)) {
            return driver.findElement(byPrivateChatContainerCssSelector).findElements(byChatMessageTextXpath).size();
        } else {
            return driver.findElements(byChatMessageTextXpath).size();
        }
    }

    @Override
    public InAMeetingScreen sendsChatMessage(String chatMessage) {
        WebElement messageTextBox, container;

        driver.waitTillElementIsPresent(byMessageTextBoxCssSelector);
        //private chat is an overlap screen on group chat (i.e. 'To: All') - hence finding the concerned text box in case of private chat
        Wait.waitFor(2);
        if (driver.isElementPresent(byPrivateChatContainerCssSelector)) {
            container = driver.waitTillElementIsPresent(byPrivateChatContainerCssSelector);
        } else {
            container = driver.waitTillElementIsPresent(byGroupChatContainerCssSelector);
        }
        messageTextBox = container.findElement(byMessageTextBoxCssSelector);
        messageTextBox.clear();
        messageTextBox.sendKeys(chatMessage);
        visually.takeScreenshot(SCREEN_NAME, "before sending the message");

        container.findElement(bySendMessageButtonCssSelector).click();
        driver.waitTillElementIsPresent(byChatMessageTextXpath); //waiting to check appearance of message in chat panel
        visually.takeScreenshot(SCREEN_NAME, "message sent");
        return this;
    }

    @Override
    public boolean isChatNotificationRedBubbleVisible() {
        return driver.isElementPresent(byChatNotificationRedBubbleXpath);
    }

    @Override
    public InAMeetingScreen userTapsOnChatIcon() {
        Wait.waitFor(1);
        enableInMeetingControls("userTapsOnChatIcon");
        driver.waitForClickabilityOf(byChatIconXpath).click();
        visually.takeScreenshot(SCREEN_NAME, "tapped on Chat Icon");
        return this;
    }

    @Override
    public boolean isChatMessageReceived(String chatMessage) {
        driver.waitForClickabilityOf(byChatMessageTextXpath, 5);
        By currentChatMessageXpath = By.xpath(CHAT_MESSAGE_XPATH.replace("@chatMessage", chatMessage));
        return driver.isElementPresent(currentChatMessageXpath);
    }

    @Override
    public InAMeetingScreen userSelectsToLeaveMeeting() {
        enableInMeetingControls("userSelectsToLeaveMeeting");
        {
            driver.waitForClickabilityOf(byLeaveButtonXpath).click();
        }
        visually.takeScreenshot(SCREEN_NAME, "userSelectsToLeaveMeeting");
        LOGGER.info("Leave button clicked");
        return this;
    }

    @Override
    public InAMeetingScreen loggedInUserClosesMeetingFeedback() {
        try {
            if (driver.waitForClickabilityOf(bySkipFeedbackButtonId, 2).isDisplayed()) {
                visually.takeScreenshot(SCREEN_NAME, "Before closing meeting feedback popup");
                driver.waitForClickabilityOf(bySkipFeedbackButtonId).click();
            }
        } catch (Exception e) {
            LOGGER.info("Feedback popup did not show up");
        }
        return this;
    }

    @Override
    public InAMeetingScreen userNavigatesToChatsTab() {
        driver.waitForClickabilityOf(CHATS_TAB_XPATH).click();
        return this;
    }

    @Override
    public InAMeetingScreen userSelectsChatSection() {
        driver.waitForClickabilityOf(byMostRecentChatWindowXpath).click();
        driver.waitForClickabilityOf(bySendButtonCssSelector);
        visually.takeScreenshot(SCREEN_NAME, "userSelectsChatSection");
        return this;
    }

    @Override
    public boolean isChatMessageReceivedInChatsTab(String chatMessage) {
        driver.waitTillElementIsPresent(byChatMessagesInChatsTabXpath);
        List<WebElement> listOfMessages = driver.findElements(byChatMessagesInChatsTabXpath);
        LOGGER.info("Total messages found on screen: " + listOfMessages.size());
        if (listOfMessages.isEmpty()) {
            return false;
        }
        for (WebElement message : listOfMessages) {
            LOGGER.info("Fetched message: " + message.getText());
            if (message.getText().trim().equals(chatMessage)) {
                return true;
            }
        }
        return false;
    }

    private void enableInMeetingControls(String calledFrom) {
        try {
            LOGGER.info(String.format("enableInMeetingControls: Called from: '%s'%n", calledFrom));
            Actions actions = new Actions(innerDriver);
            actions.moveToElement(driver.waitForClickabilityOf(byMeetingInfoIconXpath))
                    .moveByOffset(25, 25).perform();
        } catch (Exception e) {
            String logMessage = String.format(
                    "Exception occurred : enableInMeetingControls%nException: %s",
                    e.getLocalizedMessage());
            LOGGER.info(logMessage);
            ReportPortalLogger.logDebugMessage(logMessage);
        }
    }
}