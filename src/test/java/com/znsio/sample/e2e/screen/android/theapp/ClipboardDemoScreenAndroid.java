package com.znsio.sample.e2e.screen.android.theapp;

import com.context.TestExecutionContext;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.theapp.ClipboardDemoScreen;
import io.appium.java_client.MobileElement;
import org.apache.log4j.Logger;

import static com.znsio.e2e.tools.Wait.waitFor;

public class ClipboardDemoScreenAndroid
        extends ClipboardDemoScreen {
    private static final String SCREEN_NAME = ClipboardDemoScreenAndroid.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private final Driver driver;
    private final Visual visually;
    private final String bySetClipboardTextByAccessibilityId = "setClipboardText";
    private final String byMessageInputAccessibilityId = "messageInput";
    private final String byRefreshClipboardTextAccessibilityId = "refreshClipboardText";
    private final TestExecutionContext context;

    public ClipboardDemoScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        long threadId = Thread.currentThread()
                              .getId();
        this.context = Runner.getTestExecutionContext(threadId);
    }

    @Override
    public ClipboardDemoScreen setInClipboard(String content) {
        return enterTextToAddInClipboard(content).saveEnteredTextToClipBoard();
    }

    @Override
    public boolean doesAddedContentExistInClipboard() {
        String contentExpectedInClipboard = context.getTestStateAsString("contentInClipboard");

        driver.findElementByAccessibilityId(byRefreshClipboardTextAccessibilityId)
              .click();
        visually.checkWindow(SCREEN_NAME, "Clipboard refreshed");
        boolean isElementPresentByAccessibilityId = driver.isElementPresentByAccessibilityId(contentExpectedInClipboard);
        LOGGER.info(String.format("Is content present in clipboad: '%s':: '%s'", contentExpectedInClipboard, isElementPresentByAccessibilityId));
        return isElementPresentByAccessibilityId;
    }

    private ClipboardDemoScreenAndroid saveEnteredTextToClipBoard() {
        driver.findElementByAccessibilityId(bySetClipboardTextByAccessibilityId)
              .click();
        return this;
    }

    private ClipboardDemoScreenAndroid enterTextToAddInClipboard(String content) {
        waitFor(2);
        MobileElement contentElement = (MobileElement) driver.findElementByAccessibilityId(byMessageInputAccessibilityId);
        contentElement.click();
        contentElement.clear();
        contentElement.sendKeys(content);
        return this;
    }
}
