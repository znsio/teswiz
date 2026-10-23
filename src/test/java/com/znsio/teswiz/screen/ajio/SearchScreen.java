package com.znsio.teswiz.screen.ajio;


public abstract class SearchScreen {

    public static SearchScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(SearchScreen.class);
    }

    public abstract int numberOfProductFound();

    public abstract ProductScreen selectProduct();

    public abstract boolean isProductListLoaded(String product);

    public abstract String getProductListingPageHeader();

    public abstract ProductScreen selectFirstItemFromList();
}
