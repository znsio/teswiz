package com.znsio.teswiz.screen.web;

import java.util.function.Supplier;

import org.apache.commons.lang3.NotImplementedException;

import com.znsio.teswiz.web.WebEngine;

public final class WebScreenRouter {
    private WebScreenRouter() {
    }

    public static <T> T forEngine(WebEngine webEngine, String screenName, Supplier<T> seleniumSupplier,
            Supplier<T> playwrightTsSupplier) {
        return switch (webEngine) {
            case SELENIUM -> seleniumSupplier.get();
            case PLAYWRIGHT_TS -> playwrightTsSupplier.get();
            default -> throw new NotImplementedException(
                    String.format("%s is not implemented for WEB_ENGINE=%s", screenName,
                            webEngine.getConfigValue()));
        };
    }
}
