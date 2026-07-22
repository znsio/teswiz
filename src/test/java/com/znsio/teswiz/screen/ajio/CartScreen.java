package com.znsio.teswiz.screen.ajio;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.android.ajio.CartScreenAndroid;
import com.znsio.teswiz.screen.ios.ajio.CartScreenIOS;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class CartScreen {
    private static final String SCREEN_NAME = CartScreen.class.getSimpleName();
    private static final Logger LOGGER = LogManager.getLogger(SCREEN_NAME);

    public static CartScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(CartScreen.class);
    }

    public abstract String getActualProductName();
}
