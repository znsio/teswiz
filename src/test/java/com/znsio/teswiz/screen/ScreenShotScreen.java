package com.znsio.teswiz.screen;


public abstract class ScreenShotScreen {

    public static ScreenShotScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(ScreenShotScreen.class);
    }

    public abstract ScreenShotScreen takeScreenshot();
}
