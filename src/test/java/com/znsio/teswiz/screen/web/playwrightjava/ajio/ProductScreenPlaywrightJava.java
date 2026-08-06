package com.znsio.teswiz.screen.web.playwrightjava.ajio;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ajio.CartScreen;
import com.znsio.teswiz.screen.ajio.ProductScreen;

public class ProductScreenPlaywrightJava extends ProductScreen {
    private static final String ENGINE_NAME = "playwright-java";

    public ProductScreenPlaywrightJava(Driver driver, Visual visually) {
    }

    @Override
    public CartScreen addProductToCart() {
        throw unsupported();
    }

    @Override
    public String getProductName() {
        throw unsupported();
    }

    @Override
    public boolean isProductDetailsLoaded() {
        throw unsupported();
    }

    @Override
    public ProductScreen flickImage() {
        throw unsupported();
    }

    @Override
    public String isElementIdChanged() {
        throw unsupported();
    }

    @Override
    public boolean isProductBrandNameVisible() {
        throw unsupported();
    }

    @Override
    public ProductScreen clickOnAddToCart() {
        throw unsupported();
    }

    @Override
    public ProductScreen selectAvailableSize() {
        throw unsupported();
    }

    @Override
    public ProductScreen clickOnAddToBagButton() {
        throw unsupported();
    }

    @Override
    public String getAddedToBagToastMessage() {
        throw unsupported();
    }

    @Override
    public CartScreen clickOnCartIcon() {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(String.format(
                "Product details is not supported on web for WEB_ENGINE=%s.",
                ENGINE_NAME));
    }
}
