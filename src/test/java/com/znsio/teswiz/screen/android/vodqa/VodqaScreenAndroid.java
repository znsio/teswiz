package com.znsio.teswiz.screen.android.vodqa;

import com.applitools.eyes.appium.Target;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.vodqa.VodqaScreen;
import io.appium.java_client.AppiumBy;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;

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
    private final String screenSelectionXpath = "//android.view.ViewGroup[@content-desc='%s']";
    private final String swipeViewXpath = "//android.widget.TextView[@text='%s']";
    private final String swipeViewTileXpath = "//android.view.ViewGroup[@content-desc='view%s']/android.view.ViewGroup";
    private final By byNativeViewXpath = AppiumBy.xpath("//android.widget.TextView[@content-desc=\"chainedView\"]");
    private final String byPageHeaderXpath = "//android.widget.TextView[@text='%s']";

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
    public VodqaScreen tapInTheMiddle() {
        driver.waitTillElementIsVisible(byNativeViewXpath);
        visually.checkWindow(SCREEN_NAME, "Sample List page");
        driver.tapOnMiddleOfScreen();
        return this;
    }

    @Override
    public boolean isPreviousPageHeadingVisible(String pageHeading) {
        visually.checkWindow(SCREEN_NAME, "Page landed after tapping in the middle");
        return driver.isElementPresent(AppiumBy.xpath(String.format(byPageHeaderXpath, pageHeading)));
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

    @Override
    public VodqaScreen selectScreen(String screenName) {
        By byScreenNameXpath = AppiumBy.xpath(String.format(screenSelectionXpath, screenName));
        driver.waitTillElementIsPresent(byScreenNameXpath);
        driver.findElement(byScreenNameXpath).click();
        return this;
    }

    @Override
    public VodqaScreen swipeLeft() {
        driver.waitTillElementIsPresent(AppiumBy.xpath(String.format(swipeViewXpath, "1")));
        visually.check(SCREEN_NAME, "Carousel Tile before swipe left",
                Target.region(AppiumBy.xpath(String.format(swipeViewTileXpath, "1"))));
        driver.swipeLeft();
        return this;
    }

    @Override
    public boolean isSwipeSuccessful(String elementText) {
        boolean isSwipeSuccessful = driver.findElement(AppiumBy.xpath(String.format(swipeViewXpath, elementText))).isDisplayed();
        visually.check(SCREEN_NAME, "Carousel Tile after swipe", Target.region(AppiumBy.xpath(String.format(swipeViewTileXpath, elementText))));
        return isSwipeSuccessful;
    }

    @Override
    public VodqaScreen swipeRight() {
        driver.waitTillElementIsPresent(AppiumBy.xpath(String.format(swipeViewXpath, "1")));
        visually.check(SCREEN_NAME, "Carousel Tile before swipe right",
                Target.region(AppiumBy.xpath(String.format(swipeViewTileXpath, "1"))));
        driver.swipeRight();
        return this;
    }

    @Override
    public VodqaScreen swipeByPassingPercentageAttributes(int atPercentScreenHeight, int fromPercentageWidth, int toPercentScreenWidth) {
        driver.waitTillElementIsPresent(AppiumBy.xpath(String.format(swipeViewXpath, "1")));
        visually.check(SCREEN_NAME, "Carousel Tile before swipe by percentage Attributes",
                Target.region(AppiumBy.xpath(String.format(swipeViewTileXpath, "1"))));
        driver.swipeByPassingPercentageAttributes(atPercentScreenHeight, fromPercentageWidth, toPercentScreenWidth);
        return this;
    }
}
