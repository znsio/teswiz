package com.znsio.sample.e2e.screen.android.theapp;

import com.znsio.e2e.runner.Driver;
import com.znsio.e2e.runner.Visual;
import com.znsio.sample.e2e.screen.theapp.EchoScreen;
import org.openqa.selenium.By;

public class EchoScreenAndroid
        extends EchoScreen {
    private static final By bySaveMessageButtonXpath = By.xpath(
            "//android.widget.Button[@content-desc=\"messageSaveBtn\"]/android.widget.TextView");
    private static final By byGoBackToHomeScreenButtonXpath = By.xpath(
            "//android.widget.ImageButton[@content-desc=\"Navigate Up\"]");
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = EchoScreenAndroid.class.getSimpleName();
    private final String byMessageInputAccessibilityId = "messageInput";

    public EchoScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public EchoScreen echoMessage(String message) {
        driver.waitForClickabilityOf(bySaveMessageButtonXpath);
        driver.findElementByAccessibilityId(byMessageInputAccessibilityId).click();
        driver.findElementByAccessibilityId(byMessageInputAccessibilityId).sendKeys(message);
        driver.waitForClickabilityOf(bySaveMessageButtonXpath).click();
        driver.waitForClickabilityOf(byGoBackToHomeScreenButtonXpath).click();
        return this;
    }
}
