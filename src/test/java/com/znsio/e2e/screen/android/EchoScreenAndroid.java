package com.znsio.e2e.screen.android;

import com.znsio.e2e.screen.*;
import com.znsio.e2e.tools.*;
import org.openqa.selenium.*;

public class EchoScreenAndroid extends EchoScreen {
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = HomeScreenAndroid.class.getSimpleName();
    private final String byMessageInputAccessibilityId = "messageInput";
    private final By bySaveMessageButtonXpath = By.xpath("//android.view.ViewGroup[@content-desc=\"messageSaveBtn\"]/android.widget.TextView");
    private final By byGoBackToHomeScreenButtonXpath = By.xpath("/hierarchy/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.RelativeLayout/android.widget.RelativeLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.view.ViewGroup/android.widget.ImageButton");

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
