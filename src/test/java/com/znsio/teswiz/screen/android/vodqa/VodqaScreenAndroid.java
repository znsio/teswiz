package com.znsio.teswiz.screen.android.vodqa;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.vodqa.VodqaScreen;
import io.appium.java_client.AppiumBy;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;

import static com.znsio.teswiz.tools.Wait.waitFor;

public class VodqaScreenAndroid extends VodqaScreen {
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = VodqaScreenAndroid.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(VodqaScreenAndroid.class.getName());
    private final By byLoginButton = AppiumBy.xpath("//android.view.ViewGroup[@content-desc='login']/android.widget.Button");
    private final By byVerticalSwipeViewGroup = AppiumBy.xpath("//android.view.ViewGroup[@content-desc='verticalSwipe']");
    private final By byCLanguageTextView = AppiumBy.xpath("//android.widget.TextView[@text=' C']");
    private final By byRubyLanguageTextView = AppiumBy.xpath("//android.widget.TextView[@text=' Ruby']");
    private final By byJasmineLanguageTextView = AppiumBy.xpath("//android.widget.TextView[@text=' Jasmine']");

    public VodqaScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public VodqaScreen login() {
        driver.waitTillElementIsPresent(byLoginButton);
        visually.checkWindow(SCREEN_NAME, "Login Screen");
        driver.findElement(byLoginButton).click();
        return this;
    }

    @Override
    public VodqaScreen scrollFromOneElementPointToAnother() {
        driver.waitTillElementIsPresent(byVerticalSwipeViewGroup);
        visually.checkWindow(SCREEN_NAME, "Home Screen");
        driver.findElement(byVerticalSwipeViewGroup).click();
        driver.waitTillElementIsPresent(byCLanguageTextView);
        visually.checkWindow(SCREEN_NAME, "Vertical Swiping Screen Before Scroll");
        Point fromPoint = driver.findElement(byCLanguageTextView).getLocation();
        Point toPoint = driver.findElement(byRubyLanguageTextView).getLocation();
        driver.scroll(fromPoint, toPoint);
        visually.checkWindow(SCREEN_NAME, "Vertical Swiping Screen After Scroll");
        return this;
    }

    @Override
    public boolean isElementWithTextVisible() {
        return driver.isElementPresent(byJasmineLanguageTextView);
    }

    @Override
    public VodqaScreen openVerticalSwipingScreen() {
        driver.waitTillElementIsPresent(byVerticalSwipeViewGroup);
        visually.checkWindow(SCREEN_NAME, "Home Screen");
        driver.findElement(byVerticalSwipeViewGroup).click();
        LOGGER.info("vertical swiping screen is open");
        return this;
    }

    @Override
    public VodqaScreen scrollDownByScreenSize() {
        driver.waitTillElementIsPresent(byCLanguageTextView);
        driver.scrollDownByScreenSize();
        visually.checkWindow(SCREEN_NAME, "Screen scrolled down");
        return this;
    }
}