package com.znsio.teswiz.screen.ajio;


public abstract class CartScreen {

    public static CartScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(CartScreen.class);
    }

    public abstract String getActualProductName();
}
