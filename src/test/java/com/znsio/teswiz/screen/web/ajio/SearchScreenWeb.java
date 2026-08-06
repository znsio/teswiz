package com.znsio.teswiz.screen.web.ajio;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ajio.ProductScreen;
import com.znsio.teswiz.screen.ajio.SearchScreen;

public class SearchScreenWeb extends SearchScreen {
    private static final String ENGINE_NAME = "selenium";

    public SearchScreenWeb(Driver driver, Visual visually) {
    }

    @Override
    public int numberOfProductFound() {
        throw unsupported();
    }

    @Override
    public ProductScreen selectProduct() {
        throw unsupported();
    }

    @Override
    public boolean isProductListLoaded(String product) {
        throw unsupported();
    }

    @Override
    public String getProductListingPageHeader() {
        throw unsupported();
    }

    @Override
    public ProductScreen selectFirstItemFromList() {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(String.format(
                "Search results is not supported on web for WEB_ENGINE=%s.",
                ENGINE_NAME));
    }
}
