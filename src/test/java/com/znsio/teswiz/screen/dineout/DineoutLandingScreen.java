package com.znsio.teswiz.screen.dineout;


public abstract class DineoutLandingScreen {

    public static DineoutLandingScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(DineoutLandingScreen.class);
    }

    public abstract DineoutLandingScreen selectDefaultCity();

    public abstract DineoutLandingScreen selectCity(String city);

    public abstract DineoutLandingScreen searchCuisine(String cusine);
}
