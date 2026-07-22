package com.znsio.teswiz.screen.theapp;


public abstract class AppLaunchScreen {

    public static AppLaunchScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(AppLaunchScreen.class);
    }

    public abstract LoginScreen selectLogin();

    public abstract AppLaunchScreen goBack();

    public abstract EchoScreen selectEcho();

    public abstract ClipboardDemoScreen goToClipboardDemo();
}
