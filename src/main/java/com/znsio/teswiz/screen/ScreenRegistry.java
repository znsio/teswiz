package com.znsio.teswiz.screen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.web.WebEngine;
import com.znsio.teswiz.web.playwright.screen.PlaywrightTsScreenBridgeFactory;

public final class ScreenRegistry {
    private static final Logger LOGGER = LogManager.getLogger(ScreenRegistry.class.getName());
    private static final PlaywrightTsScreenBridgeFactory PLAYWRIGHT_TS_SCREEN_BRIDGE_FACTORY =
            new PlaywrightTsScreenBridgeFactory();

    private ScreenRegistry() {
    }

    public static <T> T getScreen(Class<T> screenContract) {
        long threadId = Thread.currentThread().getId();
        Driver driver = Drivers.getDriverForCurrentUser(threadId);
        Platform platform = Runner.fetchPlatform(threadId);
        Visual visually = Drivers.getVisualDriverForCurrentUser(threadId);
        WebEngine webEngine = isWebLike(platform) ? Runner.getWebEngine() : null;

        LOGGER.info("{}: Driver type: {}: Platform: {}{}",
                screenContract.getSimpleName(),
                driver.getType(),
                platform,
                null == webEngine ? "" : ": WebEngine: " + webEngine.getConfigValue());

        if (isPlaywrightTs(platform, webEngine)) {
            T bridgeScreen = PLAYWRIGHT_TS_SCREEN_BRIDGE_FACTORY.createIfSupported(screenContract, driver, visually)
                    .orElse(null);
            if (null != bridgeScreen) {
                return bridgeScreen;
            }
        }

        Class<? extends T> implementationClass = ScreenImplementationResolver.resolve(screenContract, platform,
                webEngine);
        return ScreenInstanceFactory.create(implementationClass, driver, visually);
    }

    private static boolean isWebLike(Platform platform) {
        return Platform.web.equals(platform) || Platform.electron.equals(platform);
    }

    private static boolean isPlaywrightTs(Platform platform, WebEngine webEngine) {
        return isWebLike(platform) && WebEngine.PLAYWRIGHT_TS.equals(webEngine);
    }
}
