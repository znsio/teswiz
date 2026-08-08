package com.znsio.teswiz.screen.web.playwrightjava.vodqa;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.vodqa.DragAndDropScreen;
import com.znsio.teswiz.screen.vodqa.NativeViewScreen;
import com.znsio.teswiz.screen.vodqa.VodqaScreen;
import com.znsio.teswiz.screen.vodqa.WebViewScreen;
import org.openqa.selenium.Dimension;

public class VodqaScreenPlaywrightJava extends VodqaScreen {
    private static final String FEATURE_NAME = "Vodqa";
    private static final String ENGINE_NAME = "playwright-java";

    public VodqaScreenPlaywrightJava(Driver driver, Visual visually) {
    }

    @Override
    public VodqaScreen login() {
        throw unsupported("login");
    }

    @Override
    public VodqaScreen scrollFromOneElementPointToAnother() {
        throw unsupported("scroll from one element point to another");
    }

    @Override
    public VodqaScreen tapInTheMiddle() {
        throw unsupported("tap in the middle");
    }

    @Override
    public boolean isPreviousPageHeadingVisible(String pageHeading) {
        throw unsupported("check previous page heading visibility");
    }

    @Override
    public VodqaScreen openVerticalSwipingScreen() {
        throw unsupported("open vertical swiping screen");
    }

    @Override
    public VodqaScreen scrollDownByScreenSize() {
        throw unsupported("scroll down by screen size");
    }

    @Override
    public VodqaScreen selectScreen(String screenName) {
        throw unsupported("select screen");
    }

    @Override
    public boolean isSwipeSuccessful(String tileNumber) {
        throw unsupported("check swipe success");
    }

    @Override
    public VodqaScreen swipeByPassingPercentageAttributes(int atPercentScreenHeight,
                                                          int fromPercentScreenWidth,
                                                          int toPercentScreenWidth) {
        throw unsupported("swipe by percentage attributes");
    }

    @Override
    public WebViewScreen enterIntoNewsWebViewSection() {
        throw unsupported("enter into news web view section");
    }

    @Override
    public NativeViewScreen enterIntoNativeViewSection() {
        throw unsupported("enter into native view section");
    }

    @Override
    public VodqaScreen putAppInTheBackground(int time) {
        throw unsupported("put app in the background");
    }

    @Override
    public boolean isAppWorkingInBackground() {
        throw unsupported("check app in background");
    }

    @Override
    public boolean isElementWithTextVisible(String elementText) {
        throw unsupported("check element with text visibility");
    }

    @Override
    public VodqaScreen scrollVerticallyByPercentage(int fromPercentHeight,
                                                    int toPercentHeight,
                                                    int percentWidth) {
        throw unsupported("scroll vertically by percentage");
    }

    @Override
    public VodqaScreen longPressOnElement() {
        throw unsupported("long press on element");
    }

    @Override
    public boolean isLongPressedPopupVisible() {
        throw unsupported("check long pressed popup visibility");
    }

    @Override
    public DragAndDropScreen openDragAndDropScreen() {
        throw unsupported("open drag and drop screen");
    }

    @Override
    public VodqaScreen navigateToUImageView() {
        throw unsupported("navigate to UImageView");
    }

    @Override
    public VodqaScreen pinchAndZoomInOnAnElement() {
        throw unsupported("pinch and zoom in");
    }

    @Override
    public boolean isPinchAndZoomInSuccessful(Dimension initialElementDimension) {
        throw unsupported("check pinch and zoom in success");
    }

    @Override
    public boolean isPinchAndZoomOutSuccessful(Dimension initialElementDimension) {
        throw unsupported("check pinch and zoom out success");
    }

    @Override
    public VodqaScreen pinchAndZoomOutOnAnElement() {
        throw unsupported("pinch and zoom out");
    }

    @Override
    public Dimension getImageElementDimension() {
        throw unsupported("get image element dimension");
    }

    @Override
    public VodqaScreen doubleTapOnElement() {
        throw unsupported("double tap on element");
    }

    @Override
    public boolean isDoubleTapSuccessful() {
        throw unsupported("check double tap success");
    }

    @Override
    public VodqaScreen multiTouchOnElements() {
        throw unsupported("multi touch on elements");
    }

    @Override
    public float getSliderValue() {
        throw unsupported("get slider value");
    }

    private UnsupportedOperationException unsupported(String action) {
        return new UnsupportedOperationException(String.format(
                "%s %s is not supported on web for WEB_ENGINE=%s.",
                FEATURE_NAME, action, ENGINE_NAME));
    }
}
