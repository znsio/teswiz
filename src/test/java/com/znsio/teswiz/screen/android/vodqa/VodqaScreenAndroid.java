package com.znsio.teswiz.screen.android.vodqa;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.vodqa.VodqaScreen;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;

public class VodqaScreenAndroid extends VodqaScreen {
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = VodqaScreenAndroid.class.getSimpleName();
    private final By byLoginButton = AppiumBy.xpath("//android.view.ViewGroup[@content-desc='login']/android.widget.Button");
    private final By byVerticalSwipeViewGroup = AppiumBy.xpath("//android.view.ViewGroup[@content-desc='verticalSwipe']");
    private By byLanguageTextView = AppiumBy.xpath("//android.widget.TextView[@text=' Java']");

    public VodqaScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public VodqaScreen login() {
        driver.waitTillElementIsPresent(byLoginButton);
        driver.findElement(byLoginButton).click();
        return this;
    }

    @Override
    public VodqaScreen selectVerticalSwipingTile() {
        driver.waitTillElementIsPresent(byVerticalSwipeViewGroup);
        driver.findElement(byVerticalSwipeViewGroup).click();
        return this;
    }

    @Override
    public VodqaScreen scrollFromOneElementPointToAnother(String fromElement, String toElement) {
        byLanguageTextView = AppiumBy.xpath("//android.widget.TextView[@text=' "+fromElement+"']");
        driver.waitTillElementIsPresent(byLanguageTextView);
        Point fromPoint = driver.findElement(byLanguageTextView).getLocation();
        byLanguageTextView = AppiumBy.xpath("//android.widget.TextView[@text=' "+toElement+"']");
        Point toPoint = driver.findElement(byLanguageTextView).getLocation();
        driver.scroll(fromPoint, toPoint);
        return this;
    }

    @Override
    public boolean verifyScrollSuccessOrFail(String elementText) {
        byLanguageTextView = AppiumBy.xpath("//android.widget.TextView[@text=' "+elementText+"']");
        return driver.isElementPresent(byLanguageTextView);
    }
}