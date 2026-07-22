package com.znsio.teswiz.screen;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.screen.theapp.AppLaunchScreen;
import com.znsio.teswiz.web.WebEngine;

class ScreenImplementationResolverTest {
    @Test
    void shouldResolveStandardConventionForSeleniumWebScreen() {
        Class<? extends AppLaunchScreen> implementationClass = ScreenImplementationResolver.resolve(
                AppLaunchScreen.class, Platform.web, WebEngine.SELENIUM);

        assertThat(implementationClass.getName())
                .isEqualTo("com.znsio.teswiz.screen.web.theapp.AppLaunchScreenWeb");
    }

    @Test
    void shouldResolvePlaywrightTsConventionForWebScreen() {
        Class<? extends AppLaunchScreen> implementationClass = ScreenImplementationResolver.resolve(
                AppLaunchScreen.class, Platform.web, WebEngine.PLAYWRIGHT_TS);

        assertThat(implementationClass.getName())
                .isEqualTo("com.znsio.teswiz.screen.web.playwright.theapp.AppLaunchScreenPlaywrightTs");
    }

    @Test
    void shouldResolveAndroidConventionForMobileScreen() {
        Class<? extends AppLaunchScreen> implementationClass = ScreenImplementationResolver.resolve(
                AppLaunchScreen.class, Platform.android, null);

        assertThat(implementationClass.getName())
                .isEqualTo("com.znsio.teswiz.screen.android.theapp.AppLaunchScreenAndroid");
    }
}
