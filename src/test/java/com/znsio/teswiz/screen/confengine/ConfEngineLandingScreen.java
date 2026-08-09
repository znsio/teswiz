package com.znsio.teswiz.screen.confengine;


public abstract class ConfEngineLandingScreen {

    public static ConfEngineLandingScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(ConfEngineLandingScreen.class);
    }

    public abstract ConfEngineLandingScreen getListOfConferences();
}
