package com.znsio.teswiz.screen.vodqa;


public abstract class NativeViewScreen {

    public static NativeViewScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(NativeViewScreen.class);
    }

    public abstract boolean isUserOnNativeViewScreen();
}
