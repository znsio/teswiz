package com.znsio.teswiz.screen.web.playwrightjava.ajio;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ajio.CartScreen;

public class CartScreenPlaywrightJava extends CartScreen {
    private static final String ENGINE_NAME = "playwright-java";

    public CartScreenPlaywrightJava(Driver driver, Visual visually) {
    }

    @Override
    public String getActualProductName() {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(String.format(
                "Cart is not supported on web for WEB_ENGINE=%s.",
                ENGINE_NAME));
    }
}
