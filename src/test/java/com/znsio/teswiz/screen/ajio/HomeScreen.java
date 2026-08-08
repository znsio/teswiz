package com.znsio.teswiz.screen.ajio;


import java.util.Map;

public abstract class HomeScreen {

    public static HomeScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(HomeScreen.class);
    }

    public abstract SearchScreen searchByImage();

    public abstract HomeScreen attachFileToDevice(Map imageData);

    public abstract HomeScreen goToMenu();

    public abstract SearchScreen selectProductFromCategory(String product, String category, String gender);

    public abstract ProductScreen searchForTheProduct(String productName);

    public abstract HomeScreen clickOnAllowToSendNotifications();

    public abstract HomeScreen clickOnAllowLocation();

    public abstract HomeScreen clickOnAllowLocationWhileUsingApp();

    public abstract HomeScreen relaunchApplication();
}
