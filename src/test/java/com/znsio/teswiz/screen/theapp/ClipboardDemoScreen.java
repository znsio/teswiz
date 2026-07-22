package com.znsio.teswiz.screen.theapp;


public abstract class ClipboardDemoScreen {

    public static ClipboardDemoScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(ClipboardDemoScreen.class);
    }

    public abstract ClipboardDemoScreen setInClipboard(String content);

    public abstract boolean doesAddedContentExistInClipboard();
}
