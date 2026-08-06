package com.znsio.teswiz.screen.web.ajio;

import java.util.Map;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ajio.HomeScreen;
import com.znsio.teswiz.screen.ajio.ProductScreen;
import com.znsio.teswiz.screen.ajio.SearchScreen;

public class HomeScreenWeb extends HomeScreen {
    private static final String ENGINE_NAME = "selenium";

    public HomeScreenWeb(Driver driver, Visual visually) {
    }

    @Override
    public SearchScreen searchByImage() {
        throw unsupported();
    }

    @Override
    public HomeScreen attachFileToDevice(Map imageData) {
        throw unsupported();
    }

    @Override
    public HomeScreen goToMenu() {
        throw unsupported();
    }

    @Override
    public SearchScreen selectProductFromCategory(String product, String category, String gender) {
        throw unsupported();
    }

    @Override
    public ProductScreen searchForTheProduct(String productName) {
        throw unsupported();
    }

    @Override
    public HomeScreen clickOnAllowToSendNotifications() {
        throw unsupported();
    }

    @Override
    public HomeScreen clickOnAllowLocation() {
        throw unsupported();
    }

    @Override
    public HomeScreen clickOnAllowLocationWhileUsingApp() {
        throw unsupported();
    }

    @Override
    public HomeScreen relaunchApplication() {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(String.format(
                "Home is not supported on web for WEB_ENGINE=%s.",
                ENGINE_NAME));
    }
}
