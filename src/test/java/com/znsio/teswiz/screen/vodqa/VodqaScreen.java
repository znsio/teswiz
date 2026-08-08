package com.znsio.teswiz.screen.vodqa;

import org.openqa.selenium.Dimension;

public abstract class VodqaScreen {

    public static VodqaScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(VodqaScreen.class);
    }

    public abstract VodqaScreen login();

    public abstract VodqaScreen scrollFromOneElementPointToAnother();

    public abstract VodqaScreen tapInTheMiddle();

    public abstract boolean isPreviousPageHeadingVisible(String pageHeading);

    public abstract VodqaScreen openVerticalSwipingScreen();

    public abstract VodqaScreen scrollDownByScreenSize();

    public abstract VodqaScreen selectScreen(String screenName);

    public abstract boolean isSwipeSuccessful(String tileNumber);

    public abstract VodqaScreen swipeByPassingPercentageAttributes(int atPercentScreenHeight, int fromPercentScreenWidth, int toPercentScreenWidth);

    public abstract WebViewScreen enterIntoNewsWebViewSection();

    public abstract NativeViewScreen enterIntoNativeViewSection();

    public abstract VodqaScreen putAppInTheBackground(int time);

    public abstract boolean isAppWorkingInBackground();

    public abstract boolean isElementWithTextVisible(String elementText);

    public abstract VodqaScreen scrollVerticallyByPercentage(int fromPercentHeight, int toPercentHeight, int percentWidth);

    public abstract VodqaScreen longPressOnElement();

    public abstract boolean isLongPressedPopupVisible();

    public abstract DragAndDropScreen openDragAndDropScreen();

    public abstract VodqaScreen navigateToUImageView();

    public abstract VodqaScreen pinchAndZoomInOnAnElement();

    public abstract boolean isPinchAndZoomInSuccessful(Dimension initialElementDimension);

    public abstract boolean isPinchAndZoomOutSuccessful(Dimension initialElementDimension);

    public abstract VodqaScreen pinchAndZoomOutOnAnElement();

    public abstract Dimension getImageElementDimension();

    public abstract VodqaScreen doubleTapOnElement();

    public abstract boolean isDoubleTapSuccessful();

    public abstract VodqaScreen multiTouchOnElements();

    public abstract float getSliderValue();

}

