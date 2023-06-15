package com.znsio.teswiz.screen.android.vodqa;

import com.applitools.eyes.appium.Target;
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
    private final By byCLanguageTextView = AppiumBy.xpath("//android.widget.TextView[@text=' C']");
    private final By byRubyLanguageTextView = AppiumBy.xpath("//android.widget.TextView[@text=' Ruby']");
    private final By byJasmineLanguageTextView = AppiumBy.xpath("//android.widget.TextView[@text=' Jasmine']");
    private final String screenSelectionXpath = "//android.view.ViewGroup[@content-desc='%s']";
    private final String swipeViewXpath = "//android.widget.TextView[@text='%s']";
    private final String swipeViewTileXpath = "//android.view.ViewGroup[@content-desc='view%s']/android.view.ViewGroup";

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
    public VodqaScreen selectScreen(String screenName) {
        By byScreenNameXpath = AppiumBy.xpath(String.format(screenSelectionXpath, screenName));
        driver.waitTillElementIsPresent(byScreenNameXpath);
        driver.findElement(byScreenNameXpath).click();
        return this;
    }

    @Override
    public VodqaScreen swipeLeft() {
        By bySwipeViewXpath = AppiumBy.xpath(String.format(swipeViewXpath, "1"));
        if (driver.waitTillElementIsPresent(bySwipeViewXpath).isDisplayed()) {
            driver.swipeLeft();
        }
        return this;
    }

    @Override
    public boolean verifySwipe(String elementText) {
        boolean isSwipeSuccessful = driver.findElement(AppiumBy.xpath(String.format(swipeViewXpath, elementText))).isDisplayed();
        visually.check(SCREEN_NAME, "Carousel Tile after swipe", Target.region(AppiumBy.xpath(String.format(swipeViewTileXpath, elementText))));
        return isSwipeSuccessful;
    }

    @Override
    public VodqaScreen swipeRight() {
        By bySwipeViewXpath = AppiumBy.xpath(String.format(swipeViewXpath, "1"));
        if (driver.waitTillElementIsPresent(bySwipeViewXpath).isDisplayed()) {
            driver.swipeRight();
        }
        return this;
    }

    @Override
    public VodqaScreen swipeByPassingPercentageAttributes(int atPercentScreenHeight, int fromPercentageWidth, int toPercentScreenWidth) {
        By bySwipeViewXpath = AppiumBy.xpath(String.format(swipeViewXpath, "1"));
        if (driver.waitTillElementIsPresent(bySwipeViewXpath).isDisplayed()) {
            driver.swipeByPassingPercentageAttributes(atPercentScreenHeight, fromPercentageWidth, toPercentScreenWidth);
        }
        return this;
    }
}