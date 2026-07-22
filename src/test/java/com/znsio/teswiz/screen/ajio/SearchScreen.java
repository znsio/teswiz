package com.znsio.teswiz.screen.ajio;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.ajio.SearchScreenAndroid;
import com.znsio.teswiz.screen.ios.ajio.SearchScreenIOS;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class SearchScreen {
    private static final String SCREEN_NAME = SearchScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static SearchScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(SearchScreen.class);
    }

    public abstract int numberOfProductFound();

    public abstract ProductScreen selectProduct();

    public abstract boolean isProductListLoaded(String product);

    public abstract String getProductListingPageHeader();

    public abstract ProductScreen selectFirstItemFromList();
}
