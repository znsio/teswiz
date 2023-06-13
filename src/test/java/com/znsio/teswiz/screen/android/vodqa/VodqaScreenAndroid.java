package com.znsio.teswiz.screen.android.vodqa;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.vodqa.VodqaScreen;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;

public class VodqaScreenAndroid extends VodqaScreen {
    private static final String SCREEN_NAME = VodqaScreenAndroid.class.getSimpleName();
    private final Driver driver;
    private final Visual visually;
    private final By byLoginButton = AppiumBy.xpath("//android.view.ViewGroup[@content-desc='login']/android.widget.Button");
    private final By byVerticalSwipeViewGroup = AppiumBy.xpath("//android.view.ViewGroup[@content-desc='verticalSwipe']");
    private final By byCLanguageTextView = AppiumBy.xpath("//android.widget.TextView[@text=' C']");
    private final By byRubyLanguageTextView = AppiumBy.xpath("//android.widget.TextView[@text=' Ruby']");
    private final By byJasmineLanguageTextView = AppiumBy.xpath("//android.widget.TextView[@text=' Jasmine']");
    private final By byNativeViewXpath = By.xpath("//android.widget.TextView[@content-desc=\"chainedView\"]");
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
    public boolean isPreviousPageHeaderNotVisible(String pageHeader) {
        visually.checkWindow(SCREEN_NAME, "Page landed after tapping in the middle");
        return driver.findElements(By.xpath(String.format(byPageHeaderXpath, pageHeader))).size() == 0;
    }
}
