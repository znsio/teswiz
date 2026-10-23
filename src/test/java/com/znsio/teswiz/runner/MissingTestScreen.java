package com.znsio.teswiz.screen.missing;

public abstract class MissingTestScreen {
    public static MissingTestScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(MissingTestScreen.class);
    }
}
