package com.znsio.teswiz.screen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void shouldRequireTypeScriptModuleForPlaywrightTsWebScreen() {
        assertThatThrownBy(() -> ScreenImplementationResolver.resolve(
                AppLaunchScreen.class, Platform.web, WebEngine.PLAYWRIGHT_TS))
                .isInstanceOf(com.znsio.teswiz.exceptions.InvalidTestDataException.class)
                .hasMessageContaining("WEB_ENGINE=playwright-ts requires a matching TypeScript screen module")
                .hasMessageContaining("src/test/resources/playwright/screens/theapp/app-launch.screen.ts");
    }

    @Test
    void shouldBuildPlaywrightJavaConventionForWebScreen() {
        String expectedImplementation = ScreenImplementationTarget.WEB_PLAYWRIGHT_JAVA
                .expectedClassName(AppLaunchScreen.class.getName());

        assertThat(expectedImplementation)
                .isEqualTo("com.znsio.teswiz.screen.web.playwrightjava.theapp.AppLaunchScreenPlaywrightJava");
    }

    @Test
    void shouldResolveAndroidConventionForMobileScreen() {
        Class<? extends AppLaunchScreen> implementationClass = ScreenImplementationResolver.resolve(
                AppLaunchScreen.class, Platform.android, null);

        assertThat(implementationClass.getName())
                .isEqualTo("com.znsio.teswiz.screen.android.theapp.AppLaunchScreenAndroid");
    }
}
