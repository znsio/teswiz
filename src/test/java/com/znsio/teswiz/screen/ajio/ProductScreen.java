package com.znsio.teswiz.screen.ajio;


public abstract class ProductScreen {

    public static ProductScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(ProductScreen.class);
    }

    public abstract CartScreen addProductToCart();

    public abstract String getProductName();

    public abstract boolean isProductDetailsLoaded();

    public abstract ProductScreen flickImage();

    public abstract String isElementIdChanged();

    public abstract boolean isProductBrandNameVisible();

    public abstract ProductScreen clickOnAddToCart();

    public abstract ProductScreen selectAvailableSize();

    public abstract ProductScreen clickOnAddToBagButton();

    public abstract String getAddedToBagToastMessage();

    public abstract CartScreen clickOnCartIcon();
}
